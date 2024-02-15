package com.hubspot.jinjava.util;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.PartiallyDeferredValue;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.tree.ExpressionNode;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.TextNode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeferredValueUtils {

  private static final String TEMPLATE_TAG_REGEX = "(\\w+(?:\\.\\w+)*)";
  private static final Pattern TEMPLATE_TAG_PATTERN = Pattern.compile(TEMPLATE_TAG_REGEX);

  public static boolean isFullyDeferred(Object obj) {
    return obj instanceof DeferredValue && !(obj instanceof PartiallyDeferredValue);
  }

  public static HashMap<String, Object> getDeferredContextWithOriginalValues(
    Map<String, Object> context
  ) {
    return getDeferredContextWithOriginalValues(context, ImmutableSet.of());
  }

  //The context needed for a second render
  //Ignores deferred properties with no originalValue
  //Optionally only keep keys in keysToKeep
  public static HashMap<String, Object> getDeferredContextWithOriginalValues(
    Map<String, Object> context,
    Set<String> keysToKeep
  ) {
    HashMap<String, Object> deferredContext = new HashMap<>(context.size());
    context.forEach((contextKey, contextItem) -> {
      if (keysToKeep.size() > 0 && !keysToKeep.contains(contextKey)) {
        return;
      }
      if (contextItem instanceof DeferredValue) {
        if (((DeferredValue) contextItem).getOriginalValue() != null) {
          deferredContext.put(
            contextKey,
            ((DeferredValue) contextItem).getOriginalValue()
          );
        }
      }
    });
    return deferredContext;
  }

  public static void deferVariables(String[] varTokens, Map<String, Object> context) {
    for (String varToken : varTokens) {
      String key = varToken.trim();
      Object originalValue = context.get(key);
      if (originalValue != null) {
        if (originalValue instanceof DeferredValue) {
          context.put(key, originalValue);
        } else {
          context.put(key, DeferredValue.instance(originalValue));
        }
      } else {
        context.put(key, DeferredValue.instance());
      }
    }
  }

  public static Set<String> findAndMarkDeferredProperties(Context context, Node newNode) {
    String templateSource = rebuildTemplateForNodes(newNode);
    Set<String> deferredProps = getPropertiesUsedInDeferredNodes(context, templateSource);
    Set<String> setProps = getPropertiesSetInDeferredNodes(templateSource);
    markDeferredProperties(context, Sets.union(deferredProps, setProps));
    return deferredProps;
  }

  public static Set<String> getPropertiesSetInDeferredNodes(String templateSource) {
    return findSetProperties(templateSource);
  }

  public static Set<DeferredTag> getDeferredTagsRecursively(Node deferredNode) {
    return getDeferredTags(deferredNode, 0);
  }

  public static Set<String> getPropertiesUsedInDeferredNodes(
    Context context,
    String templateSource
  ) {
    return getPropertiesUsedInDeferredNodes(context, templateSource, true);
  }

  public static Set<String> getPropertiesUsedInDeferredNodes(
    Context context,
    String templateSource,
    boolean onlyAlreadyInContext
  ) {
    Stream<String> propertiesUsed = findUsedProperties(templateSource)
      .stream()
      .map(prop -> prop.split("\\.", 2)[0]); // split accesses on .prop
    if (onlyAlreadyInContext) {
      propertiesUsed = propertiesUsed.filter(context::containsKey);
    }
    return propertiesUsed.collect(Collectors.toSet());
  }

  private static void markDeferredProperties(Context context, Set<String> props) {
    props
      .stream()
      .filter(prop -> !(context.get(prop) instanceof DeferredValue))
      .filter(prop -> !context.getMetaContextVariables().contains(prop))
      .forEach(prop -> {
        Object value = context.get(prop);
        if (value != null) {
          context.put(prop, DeferredValue.instance(value));
        } else {
          //Handle set props
          context.put(prop, DeferredValue.instance());
        }
      });
  }

  private static Set<DeferredTag> getDeferredTags(Node node, int depth) {
    // precaution - templates are parsed with this render depth so in theory the depth should never be exceeded
    Set<DeferredTag> deferredTags = getDeferredTags(node).orElse(new HashSet<>());
    int maxRenderDepth = JinjavaInterpreter.getCurrent() == null
      ? 3
      : JinjavaInterpreter.getCurrent().getConfig().getMaxRenderDepth();
    if (depth > maxRenderDepth) {
      return deferredTags;
    }
    node
      .getChildren()
      .forEach(child -> deferredTags.addAll(getDeferredTags(child, depth + 1)));
    return deferredTags;
  }

  private static String rebuildTemplateForNodes(Node node) {
    StringJoiner joiner = new StringJoiner(" ");
    getDeferredTagsRecursively(node)
      .stream()
      .map(DeferredTag::getTag)
      .forEach(joiner::add);
    return joiner.toString();
  }

  private static Set<String> findUsedProperties(String templateSource) {
    Matcher matcher = TEMPLATE_TAG_PATTERN.matcher(templateSource);
    Set<String> tags = Sets.newHashSet();
    while (matcher.find()) {
      tags.add(matcher.group(1));
    }
    return tags;
  }

  private static Set<String> findSetProperties(String templateSource) {
    Set<String> tags = Sets.newHashSet();
    String[] lines = templateSource.split("\n");
    for (String line : lines) {
      line = line.trim();
      if (line.contains(SetTag.TAG_NAME + " ")) {
        tags.addAll(findUsedProperties(line));
      }
    }

    return tags;
  }

  private static Optional<Set<DeferredTag>> getDeferredTags(Node deferredNode) {
    if (deferredNode instanceof TextNode || deferredNode.getMaster() == null) {
      return Optional.empty();
    }

    String nodeImage = deferredNode.getMaster().getImage();
    if (Strings.nullToEmpty(nodeImage).trim().isEmpty()) {
      return Optional.empty();
    }

    Set<DeferredTag> deferredTags = new HashSet<>();
    deferredTags.add(
      new DeferredTag().setTag(nodeImage).setNormalizedTag(getNormalizedTag(deferredNode))
    );

    if (deferredNode instanceof TagNode) {
      TagNode tagNode = (TagNode) deferredNode;
      if (tagNode.getEndName() != null) {
        String endTag = tagNode.reconstructEnd();
        deferredTags.add(new DeferredTag().setTag(endTag).setNormalizedTag(endTag));
      }
    }

    return Optional.of(deferredTags);
  }

  private static String getNormalizedTag(Node node) {
    if (node instanceof ExpressionNode) {
      return node.toString().replaceAll("\\s+", "");
    }

    return node.getMaster().getImage();
  }

  private static class DeferredTag {

    String tag;
    String normalizedTag;

    public String getTag() {
      return tag;
    }

    public DeferredTag setTag(String tag) {
      this.tag = tag;
      return this;
    }

    public String getNormalizedTag() {
      return normalizedTag;
    }

    public DeferredTag setNormalizedTag(String normalizedTag) {
      this.normalizedTag = normalizedTag;
      return this;
    }
  }
}
