# UI11

The goal of this project is to produce a UI toolkit for Java that is declarative and modular.

By *declarative* we mean that there's no setters on widgets, instead the changed widgets are recreated fully every
time. This results in more readable code since you don't have to worry about which property you need to update. 
Since the widget objects are lightweight, and the descendant widgets that are not changed are not notified, 
usually there are no noticeable performance impact of this approach.

By *modular* we mean that everybody can define a widget type and everybody can implement it. 
For example, a `LinearLayout`'s default behavior is to compute its children's positions and lay out them.
However, in a browser environment where we are generating HTML elements, it is overridden 
to just be translated to a CSS flexbox instead of performing the geometric calculations.

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

