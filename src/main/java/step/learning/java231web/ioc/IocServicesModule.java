package step.learning.java231web.ioc;

import com.google.inject.AbstractModule;
import step.learning.java231web.services.db.IDbService;
import step.learning.java231web.services.db.MySqlDbService;
import step.learning.java231web.services.form.IFormParseService;
import step.learning.java231web.services.form.MixedFormParseService;
import step.learning.java231web.services.hash.IHashService;
import step.learning.java231web.services.hash.Md5HashService;
import step.learning.java231web.services.kdf.IKdfService;
import step.learning.java231web.services.kdf.Pbkdf2KdfService;
import step.learning.java231web.services.storage.DiskStorageService;
import step.learning.java231web.services.storage.IStorageService;
import step.learning.java231web.services.time.ITimeService;
import step.learning.java231web.services.time.ITimestampService;
import step.learning.java231web.services.time.SystemTimeService;
import step.learning.java231web.services.token.ITokenService;
import step.learning.java231web.services.token.JwtTokenService;

/**
 * Модуль конфігурації сервісів (служб) інжектора
 * @author ilyag
 */
public class IocServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IHashService.class     ).to(Md5HashService.class   );
        bind(ITimeService.class     ).to(SystemTimeService.class);
        bind(ITimestampService.class).to(SystemTimeService.class);
        bind(IDbService.class       ).to(MySqlDbService.class   );
        bind(IKdfService.class      ).to(Pbkdf2KdfService.class );
        bind(ITokenService.class    ).to(JwtTokenService.class  );
        bind(IFormParseService.class).to(MixedFormParseService.class);
        bind(IStorageService.class  ).to(DiskStorageService.class   );
    }

}
