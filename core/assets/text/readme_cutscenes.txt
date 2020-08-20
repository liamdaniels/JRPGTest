Cutscene info
The cutscene files are effectively their own language for creating a cutscene. The cutscene manager, which follows instructions from the cutscene files, has control over the map, the text, and the player.

Here are the instructions:

INIT x*	: Calls the initCutscene() function for all sprites numbered x. Must be called to properly use sprites in a cutscene.

PMOVE x y n	: Moves the player n steps in a direction determined by x and y. For example, PMOVE 1 -1 2 moves the player right and down for 2 steps

WAIT x	: Delays time for x frames

CD d x*	: Changes direction to d for all sprites numbered x. Directions: 0 is down, 1 is up, 2 is left, 3 is right

HMOVE n d x*	: Moves all sprites numbered x for n steps, each with duration of d frames. They move forward in whatever direction they currently face

HPORT x y z	: Teleports sprite numbered z to coords x and y on the current map

RELEPORT x y z	: Teleports sprite numbered z to x steps right of the player and y steps above the player.

TEX t	: Displays text of text pointer t

ANIM a (x y)	: Displays animation a from class BattleAnimator. If x and y are given, then displays animation at coordinates x and y

