# Jinjava Releases #

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

