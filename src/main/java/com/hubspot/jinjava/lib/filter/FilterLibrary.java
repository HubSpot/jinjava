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

public class FilterLibrary extends SimpleLibrary<Filter> {

  public FilterLibrary(boolean registerDefaults) {
    super(registerDefaults);
  }

  @Override
  protected void registerDefaults() {
    registerClasses(
        AttrFilter.class,
        PrettyPrintFilter.class,

    DefaultFilter.class,
        DAliasedDefaultFilter.class,
        FileSizeFormatFilter.class,
        UrlizeFilter.class,

    BatchFilter.class,
        CountFilter.class,
        DictSortFilter.class,
        FirstFilter.class,
        GroupByFilter.class,
        JoinFilter.class,
        LastFilter.class,
        LengthFilter.class,
        ListFilter.class,
        MapFilter.class,
        RejectAttrFilter.class,
        RejectFilter.class,
        SelectFilter.class,
        SelectAttrFilter.class,
        SliceFilter.class,
        ShuffleFilter.class,
        SortFilter.class,
        SplitFilter.class,
        UniqueFilter.class,

    DatetimeFilter.class,
        DateTimeFormatFilter.class,

    AbsFilter.class,
        AddFilter.class,
        CutFilter.class,
        DivideFilter.class,
        DivisibleFilter.class,
        FloatFilter.class,
        IntFilter.class,
        Md5Filter.class,
        MultiplyFilter.class,
        RandomFilter.class,
        ReverseFilter.class,
        RoundFilter.class,
        SumFilter.class,

    EscapeFilter.class,
        EAliasedEscapeFilter.class,
        ForceEscapeFilter.class,
        StripTagsFilter.class,
        UrlEncodeFilter.class,
        XmlAttrFilter.class,

    CapitalizeFilter.class,
        CenterFilter.class,
        FormatFilter.class,
        IndentFilter.class,
        LowerFilter.class,
        TruncateFilter.class,
        TruncateHtmlFilter.class,
        UpperFilter.class,
        ReplaceFilter.class,
        StringFilter.class,
        SafeFilter.class,
        TitleFilter.class,
        TrimFilter.class,
        WordCountFilter.class,
        WordWrapFilter.class);
  }

  public Filter getFilter(String filterName) {
    return fetch(filterName);
  }

  public void addFilter(Filter filter) {
    register(filter);
  }

}
