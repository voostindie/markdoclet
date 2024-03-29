# Markdoclet

[![Build Status](https://travis-ci.com/voostindie/markdoclet.svg?branch=master)](https://travis-ci.com/voostindie/markdoclet)
[![Code Coverage](https://codecov.io/gh/voostindie/markdoclet/branch/master/graph/badge.svg)](https://codecov.io/gh/voostindie/markdoclet)
[![Known Vulnerabilities](https://snyk.io/test/github/voostindie/markdoclet/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/voostindie/markdoclet?targetFile=build.gradle)

**Update September 2021**: as I'm not using this tool anymore, I'm archiving it. Right now it's working well with modern JDKs, so it's still usable. But I'm not planning on maintaining it any longer. Who knows though?

**Update February 2021**: in order to fix a vulnerability with FreeMarker I went down a really deep rabbit hole and updated the Doclet for JDK 9+, using the updated Doclet API. This newer version doesn't work with JDK 8 or lower. I tested this version on OpenJDK 11.

This Doclet generates Markdown documentation out of Javadoc comments. Before you run outside to go jumping with joy please continue reading, because this Doclet probably doesn't do what you'd expect.

**This Doclet doesn't generate documentation out of normal Javadoc comments!**

I built this Doclet so that I can write documentation on public API's meant for non-programmers within the code itself. That saves me from duplicating the structure of these API's in a different format. The need I had was to produce API documentation that was more "UML-like", in that it mentions interfaces, attributes, operations, enumerations and constants and nothing more.

The output produced by this Doclet doesn't refer to Java code. For example, running this Doclet on the following interface:

```java
interface User {
    String getName();

    void logout();
}
```

...will produce documentation that looks like this:

    ## Interface `User`

    ### Attribute `name`: `String`

    ### Operation `logout`: `void`

## How it works

This Doclet looks only at interfaces and enumerations. Normal classes are skipped for now. Simply because I'm not interested in them at the moment.

All normal Javadoc documentation is skipped as well. To produce any kind of useful output, you need to write documentation using a special custom tag: `@md.common`.

For example:

```java
/**
 * @md.common Represents the logged-on user.
 */
interface User { }
```

All documentation after the `@md.common` tag is copied as is to the Markdown output file. So you can use any kind of Markdown formatting here.

Apart from `@md.common` you can actually use any kind of tag, as long as it starts with `@md.`. This allows you to write conditional documentation, where things have different meaning or behavior depending on the context. For example':

```java
/**
 * @md.common Represents the logged-on user.
 */
interface User {
    /**
     * @md.webshop Logs out the customer and throws away all credentials.
     * @md.intranet Does nothing. Employees are identified using SSO with smartcards. Logging out is not an option.
     */
    void logout();
}
```

To make any meaning of these tags you must provide documentation for them in a properties file. This documentation is then copied into the Markdown output wherever the custom tag appears.

For example, given this properties file:

    webshop=If the user accesses the webshop
    intranet=If the user is an employee on the internal network

...then the output for the `User` interface above would look like:

    ## Interface `User`

    Represents the logged-on user.

    ### Operation `logout`: `void`

    If the user accesses the webshop: logs out the customer and throws away all credentials.

    If the user is an employee on the internal network: does nothing. Employees are identified using SSO with smartcards. Logging out is not an option.

Sometimes it's useful to hide a certain something. Whenever that's the case, just add a `@md.hide` tag on it. Then it will be skipped completely in the Markdown output.

## How to build and run

To build, run `gradle shadowJar` Then to use, run `./markdoclet`. You can symlink to this file from somewhere in the path and then run it wherever you want.

Options:

* `-output`: Sets the output file to write the Markdown to. This is required.
* `-properties`: Sets the path to the properties file with custom tag documentation. Optional.
* `-title`: Sets the title to show on top of the output. Optional.

Apart from these options you can (or: must) provide the normal Javadoc options to make it do anything useful. For example:

    markdoclet -output test.md -sourcepath /path/to/sources nl.ulso.subpackage

See `man javadoc` for more information.
