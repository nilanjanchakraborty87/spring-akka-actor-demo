package demo.akka.spring.config;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

public class SpringActorProducer implements IndirectActorProducer {

    final ApplicationContext applicationContext;
    final String actorBeanName;
    final Object[] args;

    public SpringActorProducer(ApplicationContext applicationContext,
                               String actorBeanName, Object... args) {
        this.applicationContext = applicationContext;
        this.actorBeanName = actorBeanName;
        this.args = args;
    }

    @Override
    public Actor produce() {
        return (Actor) applicationContext.getBean(actorBeanName, args);
    }

    @Override
    public Class<? extends Actor> actorClass() {
        return (Class<? extends Actor>) applicationContext.getType(actorBeanName);
    }
}