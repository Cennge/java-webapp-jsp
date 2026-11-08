package step.learning.java231web.ioc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * ContextListener - підписник на подію створення контексту,
 * яка відповідає за запуск застосунку (див. web.xml)
 * @author ilyag
 */
public class IocContextListener extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(
                new IocServicesModule(),
                new IocServletsModule()
        );
    }

}
