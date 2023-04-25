# Jinjava Releases #
### 2023-03-03 Version 2.7.0 ([Maven Central](https://search.maven.org/artifact/com.hubspot.jinjava/jinjava/2.7.0/jar)) ###
* [Use number operations for multiply and divide filters](https://github.com/HubSpot/jinjava/pull/766)
* [Add config to require whitespace in tokens](https://github.com/HubSpot/jinjava/pull/773)
* [Make reject filter the inverse of select filter](https://github.com/HubSpot/jinjava/pull/790)
* [Make ObjectMapper configurable via JinjavaConfig](https://github.com/HubSpot/jinjava/pull/815)
* [Limit rendering cycle detection to expression nodes](https://github.com/HubSpot/jinjava/pull/817)
* [Add URL decode filter](https://github.com/HubSpot/jinjava/pull/840)
* [Fix truthiness of numbers between 0 and 1](https://github.com/HubSpot/jinjava/pull/857)
* [Fix macro function scoping inside of another macro function](https://github.com/HubSpot/jinjava/pull/869)
* [Handle thread interrupts by throwing an InterpretException](https://github.com/HubSpot/jinjava/pull/870)
* [Fix right-side inline whitespace trimming](https://github.com/HubSpot/jinjava/pull/885)
* [Fix Jinjava functionality for duplicate macro functions and call tags](https://github.com/HubSpot/jinjava/pull/889)
* [Fix custom operator precedence](https://github.com/HubSpot/jinjava/pull/902)
* [Parse leading negatives in expression nodes](https://github.com/HubSpot/jinjava/pull/896)
* [add keys function to dictionary](https://github.com/HubSpot/jinjava/pull/936)
* [Update title filter to ignore special characters](https://github.com/HubSpot/jinjava/pull/945)
* [add unescape_html filter](https://github.com/HubSpot/jinjava/pull/967)
* [Move object unwrap behavior to config object](https://github.com/HubSpot/jinjava/pull/983)
* [Get best invoke method based on parameters](https://github.com/HubSpot/jinjava/pull/996)
* [Create format_number filter](https://github.com/HubSpot/jinjava/pull/999)
* [Get current date and time from a provider](https://github.com/HubSpot/jinjava/pull/1007)
* [Create context method for checking if in for loop](https://github.com/HubSpot/jinjava/pull/1015)
* [Filter duplicate template errors](https://github.com/HubSpot/jinjava/pull/1016)
* Fix various NullPointerExceptions in filters and functions
* Various changes to reduce non-deterministic behavior
* Various changes to improve datetime formatting and exception handling
* Various PRs for eager execution to support two-phase rendering.

### 2021-10-29 Version 2.6.0 ([Maven Central](https://search.maven.org/artifact/com.hubspot.jinjava/jinjava/2.6.0/jar)) ###
* [Create interface for object truth values](https://github.com/HubSpot/jinjava/pull/747)
* [Catch concurrent modification in for loop](https://github.com/HubSpot/jinjava/pull/750)
* [Add Originating Exception Message For A TemplateSyntaxException](https://github.com/HubSpot/jinjava/pull/753)
* [Throw a template error when attempting to divide by zero](https://github.com/HubSpot/jinjava/pull/754)
* [Make unixtimestamp behave the same as System.currentTimeMillis()](https://github.com/HubSpot/jinjava/pull/755)
* [handle null argument in range function](https://github.com/HubSpot/jinjava/pull/758)
* [Track Current Processed Node In The Context](https://github.com/HubSpot/jinjava/pull/760)
* [Add Base 64 encode and decode filters](https://github.com/HubSpot/jinjava/pull/763)

### 2021-09-02 Version 2.5.10 ([Maven Central](https://search.maven.org/artifact/com.hubspot.jinjava/jinjava/2.5.10/jar)) ###
* [Make LazyExpression memoization disable-able](https://github.com/HubSpot/jinjava/pull/673)
* [Add new MapELResolver with type coercion to support accessing enum keys](https://github.com/HubSpot/jinjava/pull/688)
* Add methods to [remove error from interpreter](https://github.com/HubSpot/jinjava/pull/694),
[get the last error](https://github.com/HubSpot/jinjava/pull/695),
and [remove the last error](https://github.com/HubSpot/jinjava/pull/696)
* [Pass value of throwInterpreterErrors to child contexts](https://github.com/HubSpot/jinjava/pull/697)
* [Support Assignment Blocks with Set tags](https://github.com/HubSpot/jinjava/pull/698)
* [Handle spaces better in for loop expressions](https://github.com/HubSpot/jinjava/pull/706)
* [Support "not in"](https://github.com/HubSpot/jinjava/pull/707)
* [Set propertyResolved after evaluating the AbstractCallableMethod](https://github.com/HubSpot/jinjava/pull/708)
* [Limit infinite evaluation from recursive extends tags](https://github.com/HubSpot/jinjava/pull/719)
* [Fix striptags to clean HTML instead of parsing](https://github.com/HubSpot/jinjava/pull/733)
* Various PRs for eager execution to support two-phase rendering.

### 2021-05-21 Version 2.5.9 ([Maven Central](https://search.maven.org/artifact/com.hubspot.jinjava/jinjava/2.5.9/jar)) ###
* [fix how current paths are tracked via multiple levels of inheritance with the `{% extends %}` tag](https://github.com/HubSpot/jinjava/pull/667)

### 2021-05-20 Version 2.5.8 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.5.8%7Cjar)) ###
* Various PRs for eager execution to support two-phase rendering.
* [Add rangeLimit to JinjavaConfig](https://github.com/HubSpot/jinjava/pull/658)
* [Add namespace functionality](https://github.com/HubSpot/jinjava/pull/649)
* [Fix capitalize and title filters](https://github.com/HubSpot/jinjava/pull/661)

### 2021-04-09 Version 2.5.7 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.5.7%7Cjar)) ###
* Various PRs for support EagerTokens and two-phase rendering via ChunkResolver.
* [Preserve Raw Tags Config](https://github.com/HubSpot/jinjava/pull/518)
* [Change config name to preserveForFinalPass](https://github.com/HubSpot/jinjava/pull/520)
* [Ensure that after pushing the interpreter, it gets popped](https://github.com/HubSpot/jinjava/pull/521)
* [Python Booleans and Filter base with parsing](https://github.com/HubSpot/jinjava/pull/523)
* [toyaml/fromyaml filters](https://github.com/HubSpot/jinjava/pull/524)
* [Add ChunkResolver to partially resolve expressions](https://github.com/HubSpot/jinjava/pull/525)
* [Simply JinjavaConfig construction](https://github.com/HubSpot/jinjava/pull/528)
* [Add size limited pymaps and pylists](https://github.com/HubSpot/jinjava/pull/530)
* [Remove overrides for append and insert](https://github.com/HubSpot/jinjava/pull/536)
* [Filter upgrades to support kw params](https://github.com/HubSpot/jinjava/pull/531)
* [Check if list index is numeric before parsing to int](https://github.com/HubSpot/jinjava/pull/538)
* [Rethrow CollectionTooBigExceptions in resolver](https://github.com/HubSpot/jinjava/pull/541)
* [Add error for collection too big](https://github.com/HubSpot/jinjava/pull/550)
* [Fix args for aliased functions](https://github.com/HubSpot/jinjava/pull/551)
* [Add filter to interpret a string early](https://github.com/HubSpot/jinjava/pull/570)
* [Variable function evaluator](https://github.com/HubSpot/jinjava/pull/572)
* [Check that disabled library map isn't null](https://github.com/HubSpot/jinjava/pull/573)
* [Pyish String representations of objects](https://github.com/HubSpot/jinjava/pull/581)
* [Intial support for vsCodeTagSnippets](https://github.com/HubSpot/jinjava/pull/589)
* [Fix documentation for truncate function](https://github.com/HubSpot/jinjava/pull/594)
* [Fix bug with whitespace controls not applying properly](https://github.com/HubSpot/jinjava/pull/599)
* [Allow replace filter on non-strings](https://github.com/HubSpot/jinjava/pull/603)
* [Add function and filter to convert string to date](https://github.com/HubSpot/jinjava/pull/608)
* [Expose jinjava snippets throught the jinjava object](https://github.com/HubSpot/jinjava/pull/622)
* [Output pyish versions of objects using legacy override flag](https://github.com/HubSpot/jinjava/pull/621)
* [Trim before checking if expression is quoted](https://github.com/HubSpot/jinjava/pull/623)
* [Fix tuple parsing bug](https://github.com/HubSpot/jinjava/pull/627)
* [Fix NPE around code snippets documentation](https://github.com/HubSpot/jinjava/pull/630)

### 2020-10-07 Version 2.5.6 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.5.6%7Cjar)) ###
* [Accept ip address without network prefix in ipaddr('address') filter](https://github.com/HubSpot/jinjava/pull/512)
* [Expression test parity with jinja including isIterable](https://github.com/HubSpot/jinjava/pull/510)
* [Support IN operator for dictionaries](https://github.com/HubSpot/jinjava/pull/493)
* [Disallow adding a pyMap to itself](https://github.com/HubSpot/jinjava/pull/489)
* [Disallow adding a map to itself](https://github.com/HubSpot/jinjava/pull/474)

### 2020-06-23 Version 2.5.5 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.5.5%7Cjar)) ###
* [TagCycleException was thrown when rendering template that doesn't have any cycles](https://github.com/HubSpot/jinjava/pull/445)
* [Make global context thread-safe](https://github.com/HubSpot/jinjava/pull/445)
* [Defer variables used in deferred](https://github.com/HubSpot/jinjava/pull/449)
* [Check for nulls in range function](https://github.com/HubSpot/jinjava/pull/452)
* [Fix for "Equalto operator doesn't work in "or" statement (== does)"](https://github.com/HubSpot/jinjava/pull/455)

### 2020-05-01 Version 2.5.4 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.5.4%7Cjar)) ###
* [Remove hacky replaceL behavior](https://github.com/HubSpot/jinjava/pull/407)
* [Add over limit to template errors](https://github.com/HubSpot/jinjava/pull/412)
* [Fix several parse errors](https://github.com/HubSpot/jinjava/pull/413)
* [Add support for Custom Token Scanner Symbols](https://github.com/HubSpot/jinjava/pull/410)
* [Remove print statements from test](https://github.com/HubSpot/jinjava/pull/417)
* [Check for null Config](https://github.com/HubSpot/jinjava/pull/418)
* [Remove reference to TokenScannerSymbols in Nodes and Tokens](https://github.com/HubSpot/jinjava/pull/419)
* [Add to host blacklist for security](https://github.com/HubSpot/jinjava/pull/426)
* [Update blacklist error message copy](https://github.com/HubSpot/jinjava/pull/428)
* [Allow ELResolver to be configured](https://github.com/HubSpot/jinjava/pull/432)
* [Add interpreter to blacklist](https://github.com/HubSpot/jinjava/pull/435)

### 2020-03-06 Version 2.5.3 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.5.3%7Cjar)) ###
* [Return empty string for un-evaluated lazy expression](https://github.com/HubSpot/jinjava/pull/405)
* [Add millis precision to unixtimestamp function](https://github.com/HubSpot/jinjava/pull/399)
* [Fix implementation for slice filter](https://github.com/HubSpot/jinjava/pull/398)
* [Implement safe filter as SafeString and handle SafeString in filters, functions and expressions](https://github.com/HubSpot/jinjava/pull/394)
* [Add PyList support to ForTag](https://github.com/HubSpot/jinjava/pull/390)
* [Change DefaultFilter to implement AdvancedFilter](https://github.com/HubSpot/jinjava/pull/389)
* [Adds dict support for DefaultFilter](https://github.com/HubSpot/jinjava/pull/386)
* [Add basic deferred value support for from tag](https://github.com/HubSpot/jinjava/pull/381)
* [Fix template error line numbers](https://github.com/HubSpot/jinjava/pull/380)
* [Track dependencies in FromTag](https://github.com/HubSpot/jinjava/pull/375)
* [Lower logging level for truncate](https://github.com/HubSpot/jinjava/pull/372)
* [Handling for OutputTooBigException](https://github.com/HubSpot/jinjava/pull/371)
* [Serialize lazy expression as its underlying value](https://github.com/HubSpot/jinjava/pull/370)
* [Return image when calling toString for LazyExpression](https://github.com/HubSpot/jinjava/pull/367)
* [More supplier conversions](https://github.com/HubSpot/jinjava/pull/366)
* [Avoid tag cycles when keeping track of parent paths for blocks ](https://github.com/HubSpot/jinjava/pull/363)
* [Add python list operations to PyList](https://github.com/HubSpot/jinjava/pull/362)
* [Fix NPE with lazy expression in intermediate expression resolution](https://github.com/HubSpot/jinjava/pull/358)
* [Create new class that lazily resolves](https://github.com/HubSpot/jinjava/pull/357)
* [Upgrade map filter to advanced filter, improve error messages, and pass through args for filters](https://github.com/HubSpot/jinjava/pull/356)
* [enable more checkstyle rules](https://github.com/HubSpot/jinjava/pull/355)
* [Add codeStyleChecker](https://github.com/HubSpot/jinjava/pull/353)

### 2019-07-11 Version 2.5.2 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.5.2%7Cjar)) ###
* [Add type conversion to collection expression tests](https://github.com/HubSpot/jinjava/pull/349)
* [Change initialization of JinjavaInterpreter to instantiation](https://github.com/HubSpot/jinjava/pull/347)
* [Resolve Failure on Unknown Incompatible with default filter](https://github.com/HubSpot/jinjava/pull/345)
* [Add initial support for resolving relative paths](https://github.com/HubSpot/jinjava/pull/343)
* [Add dummy object for validation mode](https://github.com/HubSpot/jinjava/pull/341)
* [Implements equals() and hashCode() methods for TemplateError](https://github.com/HubSpot/jinjava/pull/340)

### 2019-06-07 Version 2.5.1 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.5.1%7Cjar)) ###
* [Support more ipaddr filters](https://github.com/HubSpot/jinjava/pull/338)
* [Upgrade to newer basepom](https://github.com/HubSpot/jinjava/pull/334)
* [Support empty bracket implicit index syntax](https://github.com/HubSpot/jinjava/pull/331)
* [Support nominative date formats](https://github.com/HubSpot/jinjava/pull/330)
* [Add a warning for unclosed comments](https://github.com/HubSpot/jinjava/pull/329)
* [Add a warning when there is no matching start tag for an end tag](https://github.com/HubSpot/jinjava/pull/326)
* [Add child dependency to parent dependencies](https://github.com/HubSpot/jinjava/pull/325)
* [Rewrite sort filter to address several problems](https://github.com/HubSpot/jinjava/pull/323)
* [Fix cycle reference during serialization](https://github.com/HubSpot/jinjava/pull/319)
* [Add support for resolving relative paths in separate files](https://github.com/HubSpot/jinjava/pull/316)
* [Return long value from int filter if over max int length](https://github.com/HubSpot/jinjava/pull/315)
* [Use type converter when evaulting 'in'](https://github.com/HubSpot/jinjava/pull/314)
* [Only add max depth error when not in validation mode](https://github.com/HubSpot/jinjava/pull/310)
* [Expand documentation factory with new fields](https://github.com/HubSpot/jinjava/pull/309)
* [Allow ability to set a max recursion depth in config](https://github.com/HubSpot/jinjava/pull/308)

### 2019-02-05 Version 2.5.0 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.5.0%7Cjar)) ###
* [Render node in include tag in the same interpreter scopes](https://github.com/HubSpot/jinjava/pull/301)
* [Fix expression resolver in include and from tag](https://github.com/HubSpot/jinjava/pull/300)
* [Add root and log filters](https://github.com/HubSpot/jinjava/pull/299)
* [Update expression resolver to return null instead of blank string](https://github.com/HubSpot/jinjava/pull/296)
* [Expression resolver fixed in import tag](https://github.com/HubSpot/jinjava/pull/290)
* [Error and documentation overhaul](https://github.com/HubSpot/jinjava/pull/289)
* [Allow partial evalutation of templates](https://github.com/HubSpot/jinjava/pull/282)

### 2019-02-05 Version 2.4.15 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.15%7Cjar)) ###
* [Upgrade format filter to advanced filter](https://github.com/HubSpot/jinjava/pull/279)
* [Allow null in string expression tests](https://github.com/HubSpot/jinjava/pull/278)
* [Support negative indices in list slices](https://github.com/HubSpot/jinjava/pull/276)
* [Add max string length configuration](https://github.com/HubSpot/jinjava/pull/275)
* [Removed uses of `Throwables.propagate`](https://github.com/HubSpot/jinjava/pull/272)
* [Allow tags to declare themselves safe for execution in validation mode](https://github.com/HubSpot/jinjava/pull/273)

### 2019-01-08 Version 2.4.14 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.14%7Cjar)) ###
* [Critical fix for elif statements](https://github.com/HubSpot/jinjava/pull/268)

### 2019-01-07 Version 2.4.13 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.13%7Cjar)) ###
* [Add support for expressions in selectattr and rejectattr](https://github.com/HubSpot/jinjava/pull/249)
* [Add filters for datetime arithmetic](https://github.com/HubSpot/jinjava/pull/258)
* [Add conversion to Java datetime format for strtotime](https://github.com/HubSpot/jinjava/pull/260)
* [Add set theory filters such as union, intersect and difference](https://github.com/HubSpot/jinjava/pull/262)
* [Add validation mode for extended syntax checking](https://github.com/HubSpot/jinjava/pull/264)
* [Better handling for out of range values in ranage function](https://github.com/HubSpot/jinjava/pull/265)

### 2018-11-21 Version 2.4.12 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.12%7Cjar)) ###
* [Adds regex_replace filter](https://github.com/HubSpot/jinjava/pull/252)
* [Removes some usage of Java 8 streams to fix a bytecode issue](https://github.com/HubSpot/jinjava/pull/254)

### 2018-10-23 Version 2.4.11 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.11%7Cjar)) ###
* [Add function for getting start of day](https://github.com/HubSpot/jinjava/pull/247)

### 2018-10-19 Version 2.4.10 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.10%7Cjar)) ###
* [Support negative array indices](https://github.com/HubSpot/jinjava/pull/245)

### 2018-10-15 Version 2.4.9 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.9%7Cjar)) ###
* [Add `ipaddr` filter to test valid IP addresses](https://github.com/HubSpot/jinjava/pull/237)
* [Enable nested properties for `selectattr` and `rejectattr`](https://github.com/HubSpot/jinjava/pull/238)
* [Add `do` tag to evaluate expressions without print](https://github.com/HubSpot/jinjava/pull/240)
* [Add support for timezone conversions in `datetimeformat` filter](https://github.com/HubSpot/jinjava/pull/241)
* [Add `prefix` function for `ipaddr` filter](https://github.com/HubSpot/jinjava/pull/243)

### 2018-09-07 Version 2.4.8 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.8%7Cjar)) ###
* [Bug fix for trunc division and power operations](https://github.com/HubSpot/jinjava/pull/234)

### 2018-08-31 Version 2.4.7 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.7%7Cjar)) ###
* [Do not allow returning objects of type Class](https://github.com/HubSpot/jinjava/pull/232)

### 2018-08-29 Version 2.4.6 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.6%7Cjar)) ###

* [Fix a memory leak in the global context](https://github.com/HubSpot/jinjava/pull/227)
* [Do not allow calling getClass() on objects](https://github.com/HubSpot/jinjava/pull/230)

### 2018-08-16 Version 2.4.5 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.5%7Cjar)) ###
* [Limit the number errors](https://github.com/HubSpot/jinjava/pull/222)
* [Detect fromTag cycle](https://github.com/HubSpot/jinjava/pull/221)
* [Make jinjajava interpreter render timings trackable](https://github.com/HubSpot/jinjava/pull/219)
* [Add raw object to group in groupby filter](https://github.com/HubSpot/jinjava/pull/218)
* [Register json filters](https://github.com/HubSpot/jinjava/pull/216)
* [Add filter to convert JSON string to Map](https://github.com/HubSpot/jinjava/pull/215)
* [Add filter to convert objects to JSON](https://github.com/HubSpot/jinjava/pull/213)
* [Deepen equalto expression test comparison](https://github.com/HubSpot/jinjava/pull/211)

### 2018-07-10 Version 2.4.4 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.4%7Cjar)) ###
* [Fix calling macros with kwargs](https://github.com/HubSpot/jinjava/pull/208)
* [Limit the size of strings in TemplateErrors](https://github.com/HubSpot/jinjava/pull/209)

### 2018-06-13 Version 2.4.3 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.3%7Cjar)) ###
* [Wrap items in for loop with "PyIsh" equivalents](https://github.com/HubSpot/jinjava/pull/202)

### 2018-06-01 Version 2.4.2 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.2%7Cjar)) ###
* [Upgrade jsoup to address CVE](https://github.com/HubSpot/jinjava/pull/200)

### 2018-04-22 Version 2.4.1 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.1%7Cjar)) ###

* [Use `AdvancedFilter` for `selectattr` filter](https://github.com/HubSpot/jinjava/pull/183)
* [Java 9 Support](https://github.com/HubSpot/jinjava/pull/186)
* [Adds negation for expressions ("is not") ](https://github.com/HubSpot/jinjava/pull/187)
* [Fix column numbers in syntax errors](https://github.com/HubSpot/jinjava/pull/188)
* [When reporting errors, preserve casing](https://github.com/HubSpot/jinjava/pull/189)
* [Populate `fieldName` in `TemplateSyntaxException`s](https://github.com/HubSpot/jinjava/pull/190)
* [Reintroduce stricter parsing in int and float filters](https://github.com/HubSpot/jinjava/pull/191)

### 2018-02-26 Version 2.4.0 ([Maven Central](https://search.maven.org/#artifactdetails%7Ccom.hubspot.jinjava%7Cjinjava%7C2.4.0%7Cjar)) ###

* [Make int/float parsing locale aware](https://github.com/HubSpot/jinjava/pull/178)

### 2018-02-09 Version 2.3.6 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.3.6%22)) ###

* [Add more sequence expression tests](https://github.com/HubSpot/jinjava/pull/175)
* [Don't put stack trace in the exception message](https://github.com/HubSpot/jinjava/pull/174)

### 2018-01-26 Version 2.3.5 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.3.5%22)) ###

* [Add new EscapeJinjavaFilter](https://github.com/HubSpot/jinjava/pull/168)

### 2017-11-30 Version 2.3.4 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.3.4%22)) ###

* [Preserve groupby order of elements](https://github.com/HubSpot/jinjava/pull/163)

### 2017-11-16 Version 2.3.3 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.3.3%22)) ###

* [always evaluate tags and control structures in nested expressions](https://github.com/HubSpot/jinjava/pull/161)

### 2017-11-14 Version 2.3.2 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.3.2%22)) ###

* [select filter now supports expression tests with arguments like 'equalto'](https://github.com/HubSpot/jinjava/pull/158)
* [`TemplateError`s now include a scope depth](https://github.com/HubSpot/jinjava/pull/157)

### 2017-10-30 Version 2.3.0 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.3.0%22)) ###

* Add column numbers to error messages

### 2017-10-24 Version 2.2.10 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.2.10%22)) ###

* Use code of bad syntax as field name for `TemplateSyntaxException`s

### 2017-08-31 Version 2.2.9 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.2.9%22)) ###

* Apply resolved functions, expressions, and values to all [parents of Context object](https://github.com/HubSpot/jinjava/pull/147)

### 2017-08-12 Version 2.2.8 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.2.8%22)) ###

* Prevent recursion in Jinjava.
* Fix failsOnUnknownTokens.
* Add EscapeJson filter.

### 2017-08-12 Version 2.2.7 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.2.7%22)) ###

* Delegate toString() method on PyMap

### 2017-08-03 Version 2.2.6 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.2.6%22)) ###

* Limit size of output when [building strings](https://github.com/HubSpot/jinjava/pull/137)

### 2017-08-02 Version 2.2.5 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.2.5%22)) ###

* Enable configuration of a [non-random number generator](https://github.com/HubSpot/jinjava/pull/135) for tests

### 2017-08-01 Version 2.2.4 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.2.4%22)) ###

* Allow the use of filters including upper case letters: https://github.com/HubSpot/jinjava/pull/132
* Add function to apply resolved strings from one Context object to another: https://github.com/HubSpot/jinjava/pull/133

### 2017-07-21 Version 2.2.3 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.2.3%22)) ###

* Make nested expressions configuration default to true.

### 2017-07-19 Version 2.2.2 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.2.2%22)) ###

* Disable interpretation of nested expressions with a configuration.

### 2017-06-14 Version 2.2.1 ([Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.2.1%22)) ###

* Includes field name in unknown tag error

### 2017-05-12 Version 2.2.0 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.2.0%22)) ###

* Removes `FileResourceLocator` as a default `ResourceLocator` to close a security hole. See the [README](README.md#template-loading) for details on how to reenable it.

### 2017-04-11 Version 2.1.19 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.19%22)) ###

* preserve order of named parameters

### 2017-04-10 Version 2.1.18 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.18%22)) ###

* fix bug when passing null argument to `filter`

### 2017-03-31 Version 2.1.17 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.17%22)) ###

* added config option to limit the rendered output size
* added named parameter support for filters
* added `type()` function

### 2017-03-09 Version 2.1.16 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.16%22)) ###

* disabled functions, filters and tags now add to template errors rather than throwing a fatal exception

### 2017-01-18 Version 2.1.15 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.15%22)) ###

* shaded JUEL
* added `failOnUnknownTokens` mode which is similar to Jinja's StrictUndefined

### 2016-11-18 Version 2.1.14 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.14%22)) ###

* Enabled manual whitespace control by ending or closing tags with `{%-` or `-%}`
* Fixed issue with passing arguments to `rejectattr`

### Version 2.1.13 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.13%22)) ###

* Added support for disabling specific functions, filters and tags

### Version 2.1.12 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.12%22)) ###

* Added ** and // operators
* Fixed issue with passing arguments to expression tests

### Version 2.1.11 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.11%22)) ###

* Add additional specific error enums
* Add escapeJS filter
* Allow null expressions as target of replace filter

### Version 2.1.10 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.10%22)) ###


### Version 2.1.9 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.9%22)) ###

* Exclude 'caller' from recursive macro check

### Version 2.1.8 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.8%22)) ###

* Add additional category information and error message tokens to TemplateError
* Add range function
* Update ListFilter to work with strings
* Do not allow macros to be called recursively
* Update checkstyle to 2.17

### Version 2.1.7 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.7%22)) ###

* Updated RawTag to not evaluate tags nested within it

### Version 2.1.6 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.6%22)) ###

* Added new bool filter to return boolean value from string
* Added record of expressions and values evalulated

### Version 2.1.5 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.5%22)) ###

* Add support for java.util.Optional properties, nested properties in EL expressions

### Version 2.1.4 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.4%22)) ###

* fixed ArrayIndexOutOfBoundsException when importing template with no trailing newline

### Version 2.1.3 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.3%22)) ###

* Added two new expression tests for strings: "is string_startingwith" and "is string_containing"
* Added support for multi-variable set in set tag (@amannm)
* Added new detail dimension to TemplateError: ErrorItem

### Version 2.1.2 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.2%22)) ###

* Use resolved path value in include tag cycle detection
* Store autoEscape flag in context outside of user-editable properties
* Store superBlock reference in context outside of user-editable properties
* make EL resolver read-only by default, expose as config parameter
* restrict certain methods/properties in object expressions

### Version 2.1.1 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.1%22)) ###

* Better error messages for invalid assignment in expression 
* Allow for locale-based date formatting in StrftimeFormatter
* Use configured locale for Functions.datetimeformat

### Version 2.1.0 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.1.0%22)) ###

* Refactored node render logic to return richer OutputNode instances, removing a need for a special intermediate string value in text output
* Refactored cycle detection in import and include tags to remove use of special named vars in context
* Fix bug to properly detect cycles in extends tag
* Fix infinite recursion bug in resolveBlockStubs when block contains self-reference

### Version 2.0.11 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.0.11%22)) ###

* Renaming JinjavaInterpreter.renderString() to renderFlat() to better signify its purpose
* Released build for jdk7 as version 2.0.11-java7

### Version 2.0.10 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.0.10%22)) ###

* minor performance enhancements

### Version 2.0.9 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.0.9%22)) ###

* update truncate_html filter to support preserving words by default, with an additional parameter to chop words at length
* added unique filter to remove duplicate objects from a sequence 
* add support for global trim_blocks, lstrip_blocks config settings

### Version 2.0.8 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.0.8%22)) ###

* Updated escape filter to apply to string representation of all objects, not just string instances
* Reworked variable resolution to use juel rather than custom approach
* Adding ability to track dependencies used in rendering templates
* fix issue with handling escape sequences in quoted strings within block tags

### Version 2.0.7 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.0.7%22)) ###

* Propagate interpreter errors from child interpreters
* Changed error level to WARN for invalid date format strings, invalid locale strings

### Version 2.0.6 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.0.6%22)) ###

* Fix ifchanged end tag name
* Fix implementation of autoescape tag
* Fix propagation of InterpretExceptions from tag render
* doc updates

### Version 2.0.5 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.0.5%22)) ###

* Changed behavior of date filter to be same as datetimeformat filter
* Fixed date format conversion for single-digit values, for %e, %k, %l
* Updated Jinjavadoc to support isDeprecated flag, as well as arbitrary key-value metadata

### Version 2.0.4 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.0.4%22)) ###

* Fix issue with PyishDate.isoformat()

### Version 2.0.3 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.0.3%22)) ###

* Upgrading javassist, commons-lang3 library dependencies
* Adding new factory method to Jinjava for creating a new JinjavaInterpreter instance

### Version 2.0.2 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.0.2%22)) ###

* Adding code snippets to jinjavadoc
* Adding shuffle filter for collections

### Version 2.0.1 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.0.1%22)) ###

* Adding self-documenting feature to jinjava core
* Updating addition expression operator ('+') to work with lists and dicts

### Version 2.0.0 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%222.0.0%22)) ###

* 2.0.x requires JDK 8, as it contains some critical fixes to date formatting for certain languages (i.e. Finnish months)

* The 2.0.x release has some significant refactorings in the parsing code:
** nests the .parse package under the existing .tree package
** consolidating the token scanner logic, updating the node tree parser

* future updates will be able to detect more specific template syntax errors than was previously possible.

* NodeList has been removed in favor of the native JDK LinkedList implementation


### Version 1.0.9 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%221.0.9%22)) ###

* changed how validation of object properties happens; previously, jinjava would add TemplateErrors for unknown properties at the context root (i.e. 'foo') and when the property existed but the value was null (i.e. 'foo.bar == null'). the new logic will only add an error when the property isn't found on the given base object.

* creating richer exception hierarchy for more detailed and specific error messaging

* the simple Jinjava.render(String, Map) method will now return a container exception containing all errors encountered during rendering, instead of simply the first one

### Version 1.0.8 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%221.0.8%22)) ###

* fixed issue with multiple includes of the same file in a template

### Version 1.0.7 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%221.0.7%22)) ###

* added logic to avoid include/import cycles in include and import tags

### Version 1.0.6 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%221.0.6%22)) ###

* fixed issue in strftimeformatter with format strings ending in literals
* updated expression resolver to return blank strings rather than false for exception cases

### Version 1.0.5 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%221.0.5%22)) ###

* fixed issue in sort filter when using nested attribute

### Version 1.0.4 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%221.0.4%22)) ###

* Fix issue where HelperStringTokenizer could return a null last value
* Added ```unless``` tag, for inverse-if functionality
* Implemented ```from``` tag for importing specific named variables from another context

### Version 1.0.3 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%221.0.3%22)) ###

* Adding python methods dict.update() and date.isoformat() to analogous types

### Version 1.0.2 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%221.0.2%22)) ###

* Fix issue with closure scope lookups within macro functions

### Version 1.0.1 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%221.0.1%22)) ###

* Properly implement call tag syntax
* Fix issue with declaring a list as a default method parameter value
* Adding string split built-in filter

### Version 1.0.0 ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.hubspot.jinjava%22%20AND%20v%3A%221.0.0%22)) ###

* Initial Public Release

