package model.service;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.entities.LevelData;
import model.entities.game.Rarity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import play.db.jpa.JPAApi;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NOTE: ce test suppose que JPAApi#withTransaction attend un
 * java.util.function.Function<EntityManager, T>. Si votre version de Play
 * utilise play.db.jpa.JPAApi.Function<EntityManager, T> à la place, il
 * suffit de remplacer le type générique utilisé dans le "any()" et dans le
 * cast de l'invocation ci-dessous.
 *
 * NOTE: adaptez le nom de constante Rarity.COMMON si votre enum utilise
 * d'autres libellés (ex: Rarity.COMMUN, Rarity.NORMAL, etc).
 */
public class TestGameLevelService {

    private JPAApi jpaApi;
    private Config config;
    private EntityManager em;
    private TypedQuery<LevelData> query;
    private LevelData level0;
    private LevelData level1;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        jpaApi = mock(JPAApi.class);
        config = mock(Config.class);
        em = mock(EntityManager.class);
        query = mock(TypedQuery.class);
        level0 = mock(LevelData.class);
        level1 = mock(LevelData.class);

        ConfigValue configValue = mock(ConfigValue.class);
        when(configValue.toString()).thenReturn("1.0.0");
        when(config.getValue("version")).thenReturn(configValue);

        when(em.createQuery(anyString(), eq(LevelData.class))).thenReturn(query);
        when(query.setParameter(eq("version"), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(level0, level1));

        when(jpaApi.withTransaction(ArgumentMatchers.<Function<EntityManager, List<LevelData>>>any()))
                .thenAnswer(invocation -> {
                    Function<EntityManager, List<LevelData>> fn = invocation.getArgument(0);
                    return fn.apply(em);
                });
    }

    @Test
    public void constructor_loadsLevelsFromDatabaseUsingConfiguredVersion() {
        GameLevelService service = new GameLevelService(jpaApi, config);

        assertNotNull(service);
        verify(em).createQuery(
                "SELECT s FROM LevelData s WHERE s.id.patchVersion = :version", LevelData.class);
        verify(query).setParameter("version", "1.0.0");
        verify(query).getResultList();
    }

    @Test
    public void getProba_returnsChanceForGivenLevelAndRarity() {
        when(level0.getChance(Rarity.COMMON)).thenReturn(50);
        GameLevelService service = new GameLevelService(jpaApi, config);

        int proba = service.getProba(0, Rarity.COMMON);

        assertEquals(50, proba);
        verify(level0).getChance(Rarity.COMMON);
    }

    @Test
    public void getExpRequired_returnsNextLvlForGivenLevel() {
        when(level1.getNextLvl()).thenReturn(1000);
        GameLevelService service = new GameLevelService(jpaApi, config);

        int exp = service.getExpRequired(1);

        assertEquals(1000, exp);
        verify(level1).getNextLvl();
    }

    @Test
    public void getProba_indexOutOfBounds_throwsIndexOutOfBoundsException() {
        GameLevelService service = new GameLevelService(jpaApi, config);

        assertThrows(IndexOutOfBoundsException.class,
                () -> service.getProba(99, Rarity.COMMON));
    }

    @Test
    public void getExpRequired_indexOutOfBounds_throwsIndexOutOfBoundsException() {
        GameLevelService service = new GameLevelService(jpaApi, config);

        assertThrows(IndexOutOfBoundsException.class,
                () -> service.getExpRequired(99));
    }
}