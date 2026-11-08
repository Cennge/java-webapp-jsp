package step.learning.java231web.services.kdf;

/**
 * KDF (Key Derivation Function) - формування похідного ключа (dk)
 * з пароля та солі. У БД зберігаються лише сіль та dk, не пароль
 * @author ilyag
 */
public interface IKdfService {

    /** Нова випадкова сіль (hex) */
    String newSalt();

    /** Похідний ключ (hex) для пароля з сіллю */
    String dk(String password, String salt);

}
