<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE Workset SYSTEM "http://dependency-analyzer.org/schema/dtd/workset-1.7.dtd">
<Workset version="7">
  <WorksetName>New Workset</WorksetName>
  <Options auto-reload="no" />
  <Classpath shortContainerNames="yes">
    <ClasspathPart type="bin-class">E:/SUMO/workspace/sumo library/geotools-10.4/*.jar</ClasspathPart>
  </Classpath>
  <ViewFilters>
    <PatternFilter active="yes">java.*</PatternFilter>
    <PatternFilter active="yes">javax.*</PatternFilter>
    <PatternFilter active="yes">com.sun.*</PatternFilter>
    <PatternFilter active="yes">org.xml.sax*</PatternFilter>
    <PatternFilter active="yes">org.omg.*</PatternFilter>
    <PatternFilter active="yes">org.w3c.dom.*</PatternFilter>
  </ViewFilters>
  <IgnoreFilters>
    <PatternFilter active="no">java.*</PatternFilter>
    <PatternFilter active="no">javax.*</PatternFilter>
    <PatternFilter active="no">com.sun.*</PatternFilter>
    <PatternFilter active="no">org.xml.sax*</PatternFilter>
    <PatternFilter active="no">org.omg.*</PatternFilter>
    <PatternFilter active="no">org.w3c.dom.*</PatternFilter>
  </IgnoreFilters>
  <Architecture>
    <ComponentModel name="Default" />
  </Architecture>
</Workset>