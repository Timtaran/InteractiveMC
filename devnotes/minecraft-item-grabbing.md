# Minecraft Item Grabbing

_[back to readme](/devnotes/README.md)_


My idea is to reach item in your hotbar, grab it, and you have a physical item.
In Vivecraft this can be done using mixins to inject into hotbar trackers. (Read about trackers [here](https://vivecraft.github.io/javadoc/org/vivecraft/api/client/Tracker.html)).
And someway in Visor, but I didn't look at how this can be done.

## Updating Velthoric

### Load terrain before activating physics objects in area

https://github.com/velthoric/Velthoric/issues/65

We don't want to lose our items because they fall underground, right?

### Contact Listener

First, https://github.com/velthoric/Velthoric/issues/60 needs to be implemented

Crossing JNI bridge creates A LOT of overhead, so instead of filtering everything in java, we can create native class that gonna store all notifying parameters such as and filter all collisions:

#### Body types

Every [body](https://jrouwe.github.io/JoltPhysics/class_body.html) in Jolt Physics have `UserData` that can store up to 8 bytes of data.

We can use 2 of them (65536 possible values) to store body type id. So we will have a table with all body type ids corresponding to their VxBody class.

Then in ContactListener we simply compare this 2 bytes with our required bytes.

#### Velocity

Required velocity to activate trigger.

#### Body Ids

Only notify when certain body ids are contacted.

#### Something else?

This is all what I thought about, but more filtering can be done, I think.

## Items Registry

Every addon can use InteractiveMC registries to register any item (vanilla or modded) to corresponding VxBody.

## Physical interactions

### Throwing blocks

When thrown block item touch a ground with velocity high enough, it gets placed. (and maybe "bucket! release!" ahh bs)

### Melee

Fight your enemies using physics idk