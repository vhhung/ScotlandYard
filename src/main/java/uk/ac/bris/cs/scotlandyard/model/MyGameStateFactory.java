package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import java.util.*;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);
	}
    private final class MyGameState implements GameState {
		private GameSetup setup;
	    private ImmutableSet<Piece> remaining;
	    private ImmutableList<LogEntry> log;
		private Player mrX;
	    private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;

		// Constructor of My Game State
		private MyGameState(final GameSetup setup, final ImmutableSet<Piece> remaining, final ImmutableList<LogEntry> log, final Player mrX, final List<Player> detectives){
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
			if(setup.graph.edges().isEmpty()) throw new IllegalArgumentException("Edges is empty!");
			if(!mrX.isMrX()) throw new IllegalArgumentException("This is not MrX!");
			if(detectives.isEmpty()) throw new IllegalArgumentException("Detectives is empty!");
			if(detectives.stream()
					     .map(player -> player.tickets().get(ScotlandYard.Ticket.DOUBLE))
					     .anyMatch(number -> number > 0)
			) throw new IllegalArgumentException("Detectives cannot have Double tickets!");

			if(detectives.stream()
						 .map(player -> player.tickets().get(ScotlandYard.Ticket.SECRET))
						 .anyMatch(number -> number > 0)
			) throw new IllegalArgumentException("Detectives cannot have Secret tickets!");

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
		}

		/**
		* @return the current game setup
		*/
		@Override @Nonnull public GameSetup getSetup(){
			return setup;
		}

		/**
		* @return all players in the game
		*/
		@Override @Nonnull public ImmutableSet<Piece> getPlayers(){
			Set<Piece> players = new HashSet<>();
			players.add(mrX.piece());
			for(Player detective : detectives){
				players.add(detective.piece());
			}
			return ImmutableSet.copyOf(players); // this can also be implemented by using Stream
		}

		/**
		* @param detective the detective
		* @return the location of the given detective; empty if the detective is not part of the game
		*/
		@Override @Nonnull public Optional<Integer> getDetectiveLocation(Piece.Detective detective){
			for(Player player : detectives){
				if(player.piece().equals(detective)){
					return Optional.of(player.location());
				}
			}
			return Optional.empty();
		}

		/**
		* @param piece the player piece
		* @return the ticket board of the given player; empty if the player is not part of the game
		*/
		@Override @Nonnull public Optional<TicketBoard> getPlayerTickets(Piece piece){
			if(mrX.piece().equals(piece)){
				return Optional.ofNullable(ticket -> mrX.tickets().get(ticket));
			} else {
				for(Player player : detectives){
					if(player.piece().equals(piece)){
						return Optional.ofNullable(ticket -> player.tickets().get(ticket));
					}
				}
			}
			return Optional.empty();
		}
		/**
		* @return MrX's travel log as a list of {@link LogEntry}s.
		*/
		@Override @Nonnull public ImmutableList<LogEntry> getMrXTravelLog(){
			return log;
		}
		/**
		* @return the winner of this game; empty if the game has no winners yet
		* This is mutually exclusive with {@link #getAvailableMoves()}
		*/
		@Override @Nonnull public ImmutableSet<Piece> getWinner(){
			throw new RuntimeException("Implement me!");
		}
		/**
		* @return the current available moves of the game.
		* This is mutually exclusive with {@link #getWinner()}
		*/
		@Override @Nonnull public ImmutableSet<Move> getAvailableMoves(){
			throw new RuntimeException("Implement me!");
		}

		/**
		 * Computes the next game state given a move from {@link #getAvailableMoves()} has been
		 * chosen and supplied as the parameter
		 *
		 * @param move the move to make
		 * @return the game state of which the given move has been made
		 * @throws IllegalArgumentException if the move was not a move from
		 * {@link #getAvailableMoves()}
		 */
		@Override @Nonnull public GameState advance(Move move){
			throw new RuntimeException("Implement me!");
		}
	}
}
