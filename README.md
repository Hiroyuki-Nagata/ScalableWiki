# ScalableWiki [![Build Status](https://travis-ci.org/Hiroyuki-Nagata/ScalableWiki.svg?branch=develop)](https://travis-ci.org/Hiroyuki-Nagata/ScalableWiki)

* FreeStyleWiki clone

    * Convert Perl`s HTML::Template files to Scala play template file
```
 > sbt gen-tmpl
```

* Correspondence table
  
| HTML::Template | Play Template       |
|:--------------:|:-------------------:|
| TMPL_VAR       | @value              |
| TMPL_LOOP		 | @for (e <- value)   |
| TMPL_INCLUDE	 | N/A                 |
| TMPL_IF		 | @if(value.nonEmpty) |
| TMPL_ELSE		 | } else {            |
| TMPL_UNLESS	 | @if(value.isEmpty)  |
