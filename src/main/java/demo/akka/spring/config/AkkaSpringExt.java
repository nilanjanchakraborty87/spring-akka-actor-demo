package demo.akka.spring.config;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import akka.actor.Props;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AkkaSpringExt extends AbstractExtensionId<AkkaSpringExt.SpringExt> {

    public static AkkaSpringExt AkkaSpringExtProvider = new AkkaSpringExt();

    /**
     * Is used by Akka to instantiate the Extension identified by this
     * ExtensionId, internal use only.
     */
    @Override
    public SpringExt createExtension(ExtendedActorSystem system) {
        return new SpringExt();
    }

    public static class SpringExt implements Extension {
        private ApplicationContext applicationContext;

        /**
         * Used to initialize the Spring application context for the extension.
         * @param applicationContext
         */
        public void initialize(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        /**
         * Create a Props for the specified actorBeanName using the
         * SpringActorProducer class.
         *
         * @param actorBeanName  The name of the actor bean to create Props for
         * @return a Props that will create the named actor bean using Spring
         */
        public Props props(String actorBeanName, Object... args) {
            return Props.create(SpringActorProducer.class,
                    applicationContext, actorBeanName, args);
        }

    }
}
