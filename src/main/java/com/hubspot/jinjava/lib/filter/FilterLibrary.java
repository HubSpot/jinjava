/**********************************************************************
 * Copyright (c) 2014 HubSpot Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.lib.SimpleLibrary;
import com.hubspot.jinjava.lib.filter.time.FormatDateFilter;
import com.hubspot.jinjava.lib.filter.time.FormatDatetimeFilter;
import com.hubspot.jinjava.lib.filter.time.FormatTimeFilter;
import java.util.Set;

public class FilterLibrary extends SimpleLibrary<Filter> {

  public FilterLibrary(boolean registerDefaults, Set<String> disabled) {
    super(registerDefaults, disabled);
  }

  @Override
  protected void registerDefaults() {
    registerClasses(
      AbsFilter.class,
      AddFilter.class,
      AttrFilter.class,
      Base64DecodeFilter.class,
      Base64EncodeFilter.class,
      BatchFilter.class,
      BetweenTimesFilter.class,
      BoolFilter.class,
      CapitalizeFilter.class,
      CenterFilter.class,
      CountFilter.class,
      CutFilter.class,
      DAliasedDefaultFilter.class,
      DateTimeFormatFilter.class,
      DatetimeFilter.class,
      DefaultFilter.class,
      DictSortFilter.class,
      DifferenceFilter.class,
      DivideFilter.class,
      DivisibleFilter.class,
      EAliasedEscapeFilter.class,
      EscapeFilter.class,
      EscapeJinjavaFilter.class,
      EscapeJsFilter.class,
      EscapeJsonFilter.class,
      FileSizeFormatFilter.class,
      FirstFilter.class,
      FloatFilter.class,
      ForceEscapeFilter.class,
      FormatFilter.class,
      FormatDateFilter.class,
      FormatDatetimeFilter.class,
      FormatNumberFilter.class,
      FormatTimeFilter.class,
      FromJsonFilter.class,
      FromYamlFilter.class,
      GroupByFilter.class,
      IndentFilter.class,
      IntFilter.class,
      IntersectFilter.class,
      IpAddrFilter.class,
      Ipv4Filter.class,
      Ipv6Filter.class,
      JoinFilter.class,
      LastFilter.class,
      LengthFilter.class,
      ListFilter.class,
      LogFilter.class,
      LowerFilter.class,
      MapFilter.class,
      Md5Filter.class,
      MinusTimeFilter.class,
      MultiplyFilter.class,
      PlusTimeFilter.class,
      PrettyPrintFilter.class,
      RandomFilter.class,
      RegexReplaceFilter.class,
      RejectFilter.class,
      RejectAttrFilter.class,
      RenderFilter.class,
      ReplaceFilter.class,
      ReverseFilter.class,
      RootFilter.class,
      RoundFilter.class,
      SafeFilter.class,
      SelectFilter.class,
      SelectAttrFilter.class,
      ShuffleFilter.class,
      SliceFilter.class,
      SortFilter.class,
      SplitFilter.class,
      StringFilter.class,
      StringToDateFilter.class,
      StringToTimeFilter.class,
      StripTagsFilter.class,
      SumFilter.class,
      SymmetricDifferenceFilter.class,
      TitleFilter.class,
      ToJsonFilter.class,
      ToYamlFilter.class,
      TrimFilter.class,
      TruncateFilter.class,
      TruncateHtmlFilter.class,
      UnescapeHtmlFilter.class,
      UnionFilter.class,
      UniqueFilter.class,
      UnixTimestampFilter.class,
      UpperFilter.class,
      UrlDecodeFilter.class,
      UrlEncodeFilter.class,
      UrlizeFilter.class,
      WordCountFilter.class,
      WordWrapFilter.class,
      XmlAttrFilter.class
    );
  }

  public Filter getFilter(String filterName) {
    return fetch(filterName);
  }

  public void addFilter(Filter filter) {
    register(filter);
  }
}
