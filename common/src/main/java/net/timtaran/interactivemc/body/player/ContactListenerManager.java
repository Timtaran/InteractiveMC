package net.timtaran.interactivemc.body.player;

import com.github.stephengold.joltjni.Body;
import com.github.stephengold.joltjni.CustomContactListener;
import com.github.stephengold.joltjni.SubShapeIdPair;
import com.github.stephengold.joltjni.enumerate.ValidateResult;
import net.xmx.velthoric.core.physics.world.VxPhysicsWorld;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ContactListenerManager {
    public static final List<Reject> hashMap = new CopyOnWriteArrayList<>();
    public static final List<Notify> notifyList = new CopyOnWriteArrayList<>();
    public static final List<Notify> notifyOnRemovalList = new CopyOnWriteArrayList<>();

    public static void init(VxPhysicsWorld world) {
        // todo implement as native method in velthoric with contact listeners register system to avoid overhead
        world.getPhysicsSystem().setContactListener(new CustomContactListener() {
            @Override
            public void onContactRemoved(long pairVa) {
                try (SubShapeIdPair pair = new SubShapeIdPair(pairVa)) {
                    for (Notify notify : notifyOnRemovalList) {
                        if ((pair.getBody1Id() == notify.body1 && pair.getBody2Id() == notify.body2) || (pair.getBody1Id() == notify.body2 && pair.getBody2Id() == notify.body1)) {
                            notify.operation().execute(notify.body1, notify.body2);
                        }
                    }
                }
            }

            @Override
            public int onContactValidate(long body1Va, long body2Va, double baseOffsetX, double baseOffsetY, double baseOffsetZ, long collisionResultVa) {
                int body1id = new Body(body1Va).getId();
                int body2id = new Body(body2Va).getId();

                for (Notify notify : notifyList) {
                    if ((body1id == notify.body1 && body2id == notify.body2) || (body1id == notify.body2 && body2id == notify.body1)) {
                        notify.operation().execute(body1id, body2id);
                    }
                }

                for (Reject reject : hashMap) {
                    if ((body1id == reject.body1 && body2id == reject.body2) || (body1id == reject.body2 && body2id == reject.body1)) {
                        return ValidateResult.RejectContact.ordinal();
                    }
                }
                return ValidateResult.AcceptContact.ordinal();
            }
        });
    }
    public interface Operation {
        void execute(int bodyid1, int bodyid2);
    }

    public record Reject(int body1, int body2) {
    }

    public record Notify(int body1, int body2, Operation operation) {
    }
}
