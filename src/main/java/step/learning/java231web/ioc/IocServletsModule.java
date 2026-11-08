package step.learning.java231web.ioc;

import com.google.inject.servlet.ServletModule;
import step.learning.java231web.filters.AuthFilter;
import step.learning.java231web.filters.CorsFilter;
import step.learning.java231web.filters.EncodingFilter;
import step.learning.java231web.servlets.AboutServlet;
import step.learning.java231web.servlets.AdminServlet;
import step.learning.java231web.servlets.BackServlet;
import step.learning.java231web.servlets.CatalogServlet;
import step.learning.java231web.servlets.DbServlet;
import step.learning.java231web.servlets.GsonServlet;
import step.learning.java231web.servlets.HomeServlet;
import step.learning.java231web.servlets.PrivacyServlet;
import step.learning.java231web.servlets.ProductSectionServlet;
import step.learning.java231web.servlets.ProductServlet;
import step.learning.java231web.servlets.SignupServlet;
import step.learning.java231web.servlets.StatusServlet;
import step.learning.java231web.servlets.StorageServlet;
import step.learning.java231web.servlets.UserServlet;

/**
 * Модуль конфігурації сервлетів (і фільтрів) інжектора
 * @author ilyag
 */
public class IocServletsModule extends ServletModule {

    @Override
    protected void configureServlets() {
        // З переходом на IoC необхідно зняти всі @Web-анотації
        // з фільтрів та сервлетів, а також прибрати їх з web.xml.
        // До фільтрів та сервлетів НЕОБХІДНО додати анотацію @Singleton
        filter("/*").through( EncodingFilter.class );
        filter("/*").through( CorsFilter.class     );
        filter("/*").through( AuthFilter.class     );

        serve("/"       ).with( HomeServlet.class    );
        serve("/about"  ).with( AboutServlet.class   );
        serve("/privacy").with( PrivacyServlet.class );
        serve("/gson"   ).with( GsonServlet.class    );
        serve("/db"     ).with( DbServlet.class      );
        serve("/back"   ).with( BackServlet.class    );
        serve("/signup" ).with( SignupServlet.class  );
        serve("/status" ).with( StatusServlet.class  );
        serve("/user",    "/user/*"   ).with( UserServlet.class           );
        serve("/admin",   "/admin/*"  ).with( AdminServlet.class          );
        serve("/section", "/section/*").with( ProductSectionServlet.class );
        serve("/product", "/product/*").with( ProductServlet.class        );
        serve("/storage/*"            ).with( StorageServlet.class        );
        serve("/catalog", "/catalog/*").with( CatalogServlet.class        );
    }

}
