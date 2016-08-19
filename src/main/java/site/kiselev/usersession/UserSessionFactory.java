package site.kiselev.usersession;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.kiselev.ApplicationConfig;
import site.kiselev.datastore.Datastore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 * UserSessionFactory
 */
public class UserSessionFactory {

    private LoadingCache<Integer, UserSession> cache;

    private final Logger logger = LoggerFactory.getLogger(UserSessionFactory.class);

    private RemovalListener<Integer, UserSession> removalListener = removal -> {
        UserSession userSession = removal.getValue();
        assert userSession != null;
        userSession.exit();
    };

    public UserSessionFactory(Datastore datastore, BlockingQueue<SendMessage> outputQueue) {
        logger.debug("Creating new UserSessionFactory");
        CacheLoader<Integer, UserSession> loader = new CacheLoader<Integer, UserSession>() {
            @Override
            public UserSession load(Integer userID) throws Exception {
                return new UserSession(datastore, outputQueue, userID);
            }
        };
        cache = buildCache(loader);
    }

    public UserSessionFactory(CacheLoader<Integer, UserSession> otherLoader) {
        logger.debug("Creating new UserSessionFactory with custom loader");
        cache = buildCache(otherLoader);
    }

    public class UserSessionFactoryException extends Exception {
        UserSessionFactoryException(Exception e) {
            super(e);
        }
    }

    private LoadingCache<Integer, UserSession> buildCache(CacheLoader<Integer, UserSession> loader) {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(ApplicationConfig.EXPIRE_AFTER_TIMEOUT, ApplicationConfig.EXPIRE_AFTER_TIMEOUT_TIME_UNIT)
                .removalListener(removalListener)
                .build(loader);
    }


    public UserSession getUserSession(Integer userID) throws UserSessionFactoryException {
        logger.trace("Getting UserSession for user {}", userID);
        try {
            return cache.get(userID);
        } catch (ExecutionException e) {
            logger.error("Can't get UserSession for userID {}: {}", userID, e);
            throw new UserSessionFactoryException(e);
        }
    }
}
