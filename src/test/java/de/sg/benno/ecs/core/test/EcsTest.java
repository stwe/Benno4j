package de.sg.benno.ecs.core.test;

import de.sg.benno.ecs.core.Component;
import de.sg.benno.ecs.core.Ecs;
import de.sg.benno.ecs.core.EntitySystem;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EcsTest {

    @org.junit.jupiter.api.Test
    void testEcs() {

        class Position implements Component {}
        class Transform implements Component {}
        class Health implements Component {}

        ArrayList<Class<? extends Component>> componentTypes = new ArrayList<>();
        componentTypes.add(Position.class);
        componentTypes.add(Transform.class);
        componentTypes.add(Health.class);

        var ecs = new Ecs(componentTypes);
        var em = ecs.getEntityManager();

        var e0 = em.createEntity();
        e0.addComponent(new Transform());
        e0.addComponent(new Health());

        var e1 = em.createEntity();
        e1.addComponent(new Position());
        e1.addComponent(new Health());

        var e2 = em.createEntity();
        e2.addComponent(new Transform());
        e2.addComponent(new Health());

        class MoveSystem extends EntitySystem {

            public MoveSystem(Ecs ecs, int priority, Class<? extends Component>... signatureComponentTypes) {
                super(ecs, priority, signatureComponentTypes);
            }

            @Override
            public void init(Object... params) throws Exception {

            }

            @Override
            public void input() {

            }

            @Override
            public void update() {
                for (var entity : getEcs().getEntityManager().getEntities(getSignature())) {
                    System.out.println("update MoveSystem");
                }
            }

            @Override
            public void render() {

            }

            @Override
            public void cleanUp() {

            }
        }

        var moveSystem = new MoveSystem(ecs, 0, Transform.class, Health.class);
        ecs.addSystem(moveSystem);

        System.out.println(Ecs.getBitsString(e0.getSignatureBitSet()));
        System.out.println(Ecs.getBitsString(e1.getSignatureBitSet()));
        System.out.println(Ecs.getBitsString(e2.getSignatureBitSet()));

        System.out.println(Ecs.getBitsString(moveSystem.getSignature().getSignatureBitSet()));


        ecs.update();

        var t = 0;
    }
}
