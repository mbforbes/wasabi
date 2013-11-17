Features

* Implement collision / non-collision layers

* Implement character layers (place hero, place enemies)

* Implement level saving / loading (how to pickle? easy serialize?)

* Modes: insert, edit, delete

* Implement enemy collision mechanics (die, restart)

* Upgrade level editor UI for layer view, primitive buttons

* Implement attacking mechanic

 0. Animation
 0. Switch to bald sprite
 0. Hat physics (behavior? avoid GC by having only 1 hat obj!)
 0. Pick up hat
 0. Enemy / hat interactions

* Implement parallax layers

* Implement 'properties' window of level editor

Optimization
* Replace all SpriteBatch bach.begin() and batch.end() method calls with a wrapper that logs when this happens and what viewport it was called from, and then see if there are any consecutive begin/end calls that can be refactored. Wawnt to avoid doing so many. Also, I *should* be able to change the GL20's glViewport within a batch's drawing and avoid more than just one of these...

* Log Java GC calls

* Log memory