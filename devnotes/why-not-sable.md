# Why Velthoric and not Sable?

_[back to readme](/devnotes/README.md)_

## Why Velthoric?

My initial plan was to implement my own physics engine following the Ian Millington's book,
but from some yt video I found Velthoric, it were looking good, so that's it.

Velthoric still needs to be polished, so I have looked into Sable when it was released.

## Why not Sable?

I simply don't think InteractiveMC will work with Sable sublevels system. I have asked about custom non-block items in Sable discord (tbh, only once, and haven't reasked) and got no satisfying answer, took a look at [wiki](https://github.com/ryanhcode/sable/wiki) and took a look at Sable add-ons (especially [Player RagDolls](https://github.com/Leo-T22/sable-player-ragdoll/blob/main/src/main/java/dev/leo/sableplayerragdoll/block/RagdollBlocks.java)).

And it seems that the only way to create your own physical bodies is to create Minecraft blocks and work with them.

And I think this is inconvenient. I look at Player RagDolls add-on code and this is not what I want to deal with, neither most people that might want to create simple addon without complicated stuff.

I might be wrong, and I probably will take a deeper look when I finish my break, but this is what I think for now.