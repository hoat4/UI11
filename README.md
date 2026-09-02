# UI11

The goal of this project is to produce a UI toolkit for Java with the following characteristics:
- it should be declarative so there are no setters on widgets, instead the changed widgets are recreated fully every
time. This results in more readable code since you don't have to worry about which property you need to update. 
Since the widget objects are lightweight, and the descendant widgets that are not changed are not notified, 
usually there are no noticeable performance impact of this approach.
- it should be extensible, so builtin widgets don't have a special role, anybody can define a
widget type that looks similar to the builtin widgets from the outside
- it should be possible to implement various rendering targets, such as:
  - drawing to a GPU surface, as in desktop and mobile operating systems (or WebGL/WebGPU)
  - copying continously to a scene graph, such as DOM in JavaScript
  - static document with layout (HTML, docx?)
  - static document without layout (SVG, PDF)
  - non-pixel based screens, such as character-based displays ("console")
- framerate should match display output's native framerate if the underlying platform makes it 
  possible (e.g. on some platforms, it is almost impossible to display new large images without 
  skipping some frames)  

### Current platform integrations

- `platform-awt`: Uses AWT for managing windows and Java2D for rendering
- `platform-dom`: Uses TeaVM JSO DOM APIs for displaying widgets. Only applicable if running in a TeaVM-transpiled
  code in a browser.
- `platform-html` (future): Writes widget trees as static HTML files.
- `platform-glass`: Uses the Glass abstraction from JavaFX for managing windows and renders content through
  the `renderer-opengles` module.

### Simplest example

This opens a new window with a text, using the platform provider that is firstly available on the classpath:
``Window.open(new Text("Hello world!"));``

