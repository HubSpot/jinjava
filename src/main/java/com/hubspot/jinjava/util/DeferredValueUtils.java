package com.hubspot.jinjava.util;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.ExpressionNode;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.TextNode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DeferredValueUtils {
  private static final Pattern TEMPLATE_TAG_PATTERN = Pattern.compile(
    "(\\w+(?:\\.\\w+)*)"
  );

  public static HashMap<String, Object> getDeferredContextWithOriginalValues(
    Context context
  ) {
    return getDeferredContextWithOriginalValues(context, ImmutableSet.of());
  }

  //The context needed for a second render
  //Ignores deferred properties with no originalValue
  //Optionally only keep keys in keysToKeep
  public static HashMap<String, Object> getDeferredContextWithOriginalValues(
    Context context,
    Set<String> keysToKeep
  ) {
    HashMap<String, Object> deferredContext = new HashMap<>(context.size());
    context.forEach(
      (contextKey, contextItem) -> {
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
        } else {
          deferredContext.put(contextKey, contextItem);
        }
      }
    );
    return deferredContext;
  }

  public static void markDeferredProperties(Context context, Set<String> props) {
    props
      .stream()
      .filter(prop -> !(context.get(prop) instanceof DeferredValue))
      .forEach(prop -> context.put(prop, DeferredValue.instance(context.get(prop))));
  }

  public static Set<String> getPropertiesUsedInDeferredNodes(Context context) {
    String templateSource = rebuildTemplateForNodes(context.getDeferredNodes());
    Set<String> propertiesUsed = findUsedProperties(templateSource);
    return propertiesUsed
      .stream()
      .map(prop -> prop.split("[\\[.]", 2)[0]) // split map accesses on .prop or ['prop']
      .filter(context::containsKey)
      .collect(Collectors.toSet());
  }

  private static String rebuildTemplateForNodes(Set<Node> nodes) {
    StringJoiner joiner = new StringJoiner(" ");
    getDeferredTags(nodes).stream().map(DeferredTag::getTag).forEach(joiner::add);
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

  public static Set<DeferredTag> getDeferredTags(Set<Node> deferredNodes) {
    return getDeferredTags(new LinkedList<>(deferredNodes), 0);
  }

  private static Set<DeferredTag> getDeferredTags(List<Node> nodes, int depth) {
    // precaution - templates are parsed with this render depth so in theory the depth should never be exceeded
    Set<DeferredTag> deferredTags = new HashSet<>();
    int maxRenderDepth = JinjavaInterpreter.getCurrent() == null
      ? 3
      : JinjavaInterpreter.getCurrent().getConfig().getMaxRenderDepth();
    if (depth > maxRenderDepth) {
      return deferredTags;
    }
    for (Node node : nodes) {
      getDeferredTags(node).ifPresent(deferredTags::addAll);
      deferredTags.addAll(getDeferredTags(node.getChildren(), depth + 1));
    }
    return deferredTags;
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
