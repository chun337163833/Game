package com.whiuk.philip.mmorpg.server.game;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.whiuk.philip.mmorpg.shared.Messages.ClientMessage.GameData;
import com.whiuk.philip.mmorpg.shared.Messages.ClientMessage.GameData.ActionInformation;
import com.whiuk.philip.mmorpg.shared.Messages.ClientMessage.GameData.CombatInformation;
import com.whiuk.philip.mmorpg.shared.Messages.ClientMessage.GameData.MovementInformation;
import com.whiuk.philip.mmorpg.shared.Messages.ServerMessage;
import com.whiuk.philip.mmorpg.server.MessageHandlerService;
import com.whiuk.philip.mmorpg.server.auth.AuthService;
import com.whiuk.philip.mmorpg.server.system.InvalidMappingException;
import com.whiuk.philip.mmorpg.server.watchdog.WatchdogService;
import com.whiuk.philip.mmorpg.serverShared.Account;

/**
 * Game Service implementation.
 * 
 * @author Philip Whitehouse
 */
@Service
public class GameServiceImpl implements GameService {
    /**
	 *
	 */
    private static final Logger LOGGER = Logger
            .getLogger(GameServiceImpl.class);
    /**
     * Authentication service.
     */
    @Autowired
    private AuthService authService;
    /**
     * Message handler.
     */
    @Autowired
    private MessageHandlerService messageHandlerService;
    /**
     * Character -> Account mapping.
     */
    private Map<GameCharacter, Account> accounts;
    /**
     * Account -> Character mapping.
     */
    private Map<Account, GameCharacter> characters;

    /**
     * Watchdog service.
     */
    @Autowired
    private WatchdogService watchdogService;

    /**
     * Game world.
     */
    private GameWorld gameWorld;

    /**
     * Initialize the service.
     */
    @PostConstruct
    public final void init() {
        gameWorld = GameWorld.load();
        watchdogService.monitor(gameWorld);
    }

    @Override
    public final void processMessage(final Account account, final GameData data) {
        if (data.getType() == GameData.Type.CHARACTER_SELECTION) {
            characterSelection(account, data);
        } else if (data.getType() == GameData.Type.EXIT) {
            if (accounts.get(account) != null) {
                handleLogout(account);
            }
        } else if (characters.get(account) != null) {
            update(account, data);
        } else {
            handleActionInInvalidState(account, data);
        }
    }

    /**
     * Logs action attempted when no character selected.
     * @param account Account
     * @param data Data
     */
    private void handleActionInInvalidState(
            final Account account, final GameData data) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Handle a logout message.
     * @param account
     */
    private void handleLogout(final Account account) {
        // TODO Auto-generated method stub
    }

    /**
     * Handle client game update.
     * 
     * @param account Account
     * @param data
     */
    private void update(final Account account, final GameData data) {
        switch (data.getType()) {
            case MOVEMENT:
                move(characters.get(account), data.getMovementInformation());
                break;
            case ACTION:
                action(characters.get(account), data.getActionInformation());
                break;
            case COMBAT:
                combat(characters.get(account), data.getCombatInformation());
                break;
            default:
                try {
                    messageHandlerService.handleUnknownMessageType(authService
                            .getConnection(account));
                } catch (InvalidMappingException e) {
                    LOGGER.info("Unknown connection from account " + account
                            + " sent unknown game message type:"
                            + data.getType());
                }
        }
    }

    /**
     * Handle character selection messages.
     * 
     * @param account Account
     * @param data
     */
    private void characterSelection(final Account account, final GameData data) {
        if (characters.get(account) != null) {
            ServerMessage message = ServerMessage
                    .newBuilder()
                    .setType(ServerMessage.Type.GAME)
                    .setGameData(
                            ServerMessage.GameData
                                    .newBuilder()
                                    .setError(
                                            ServerMessage.GameData.Error.CHARACTER_ALREADY_SELECTED))
                    .build();
            messageHandlerService.queueOutboundMessage(message);
        } else {
            loadCharacters(account);
        }
    }

    /**
     * Load character.
     * @param account Account
     */
    private void loadCharacters(final Account account) {
        // TODO Auto-generated method stub
    }

    /**
     * Handles character combat messages.
     * 
     * @param character
     * @param combatInformation
     */
    private void combat(final GameCharacter character,
            final CombatInformation combatInformation) {
        // TODO Auto-generated method stub

    }

    /**
     * Handles action messages.
     * 
     * @param character
     * @param actionInformation
     */
    private void action(final GameCharacter character,
            final ActionInformation actionInformation) {
        // TODO Auto-generated method stub

    }

    /**
     * Handles character movement messages.
     * 
     * @param character
     * @param movementInformation
     */
    private void move(final GameCharacter character,
            final MovementInformation movementInformation) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void notifyLogout(final Account account) {
        GameCharacter c = characters.remove(account);
        accounts.remove(c);
        c.logout();
    }

}
