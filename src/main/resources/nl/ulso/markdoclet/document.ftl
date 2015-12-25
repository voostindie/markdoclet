<#-- @ftlvariable name="document" type="nl.ulso.markdoclet.document.Document" -->
<#-- @ftlvariable name="dateGenerated" type="java.lang.String" -->
<#macro paragraphs section>
    <#list section.paragraphs as paragraph>
    <#if document.getHeader(paragraph.type)?length != 0>
${document.getHeader(paragraph.type)}: ${paragraph.contents?uncap_first}

    <#else>
${paragraph.contents}

    </#if>
    </#list>
</#macro>
# ${document.title}

This document was generated on ${dateGenerated}.

<#list document.interfaces as interface>
## Interface `${interface.name}`

<@paragraphs interface/>
    <#if interface.attributes??>
        <#list interface.attributes as attribute>
### Attribute `${attribute.name?uncap_first}`: `${attribute.type}`

<@paragraphs attribute/>
        </#list>
    </#if>
    <#if interface.operations??>
        <#list interface.operations as operation>
### Operation `${operation.name}`: `${operation.returnType}`
            <#if operation.parameters?size != 0>

Parameters:

                <#list operation.parameters as parameter>
* `${parameter.name}`: `${parameter.type}`
                </#list>
            </#if>

<@paragraphs operation/>
        </#list>
    </#if>
</#list>
<#list document.enumerations as enumeration>
## Enumeration `${enumeration.name}`

<@paragraphs enumeration/>
    <#list enumeration.constants as constant>
### Constant `${constant.name}`

<@paragraphs constant/>
    </#list>
</#list>