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
			//Check if the moves is emptpy
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");

			//Check if edges of the graph is empty
			if(setup.graph.edges().isEmpty()) throw new IllegalArgumentException("Edges is empty!");

			//Check if nodes of the graph is empty
			if(setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Nodes is empty!");

			//Check if there is no mrX
			if(!mrX.isMrX()) throw new IllegalArgumentException("There is no  MrX!");

			//Check if the color of mrX is not black
			if(!mrX.piece().webColour().equals("#000")) throw new IllegalArgumentException("MrX is not a black piece!");

			//Check if the detectives is empty
			if(detectives.isEmpty()) throw new IllegalArgumentException("Detectives is empty!");

			// Check is there any duplicated location
			// Check is there any detective having Double ticket or Secret ticket
			// Check if there is mrX in detectives list
			// Time complexity: O(n)
			List<Integer> locations = new ArrayList<>();
			for(Player detective : detectives){
				int location = detective.location();
				if(locations.contains((Integer)location)) throw new IllegalArgumentException("Detectives cannot have duplicate locations!");
				locations.add(location);
				if(detective.tickets().getOrDefault(ScotlandYard.Ticket.SECRET, 0) != 0){
					throw new IllegalArgumentException("Detectives cannot have Secret tickets!");
				}
				if(detective.tickets().getOrDefault(ScotlandYard.Ticket.DOUBLE, 0) != 0){
					throw new IllegalArgumentException("Detectives cannot have Double tickets!");
				}
				if(detective.equals(mrX)) throw new IllegalArgumentException("Detectives cannot have MrX!");
			}

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
				return Optional.of(ticket -> mrX.tickets().get(ticket));
			} else {
				for(Player player : detectives){
					if(player.piece().equals(piece)){
						return Optional.of(ticket -> player.tickets().get(ticket));
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
			return ImmutableSet.of();
		}

		/**
		 * Implement Helper function: SingleMove
		 */
		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){
		  // TODO create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
		  Set<Move.SingleMove> moves = new HashSet<>();

		  for(int destination : setup.graph.adjacentNodes(source)) {
			// TODO find out if destination is occupied by a detective
			//  if the location is occupied, don't add to the collection of moves to return
			// Using Stream to check if the location if occupied
			if (detectives.stream().anyMatch(detective -> detective.location() == destination)) continue;

			for(ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
			  // TODO find out if the player has the required tickets
			  //  if it does, construct a SingleMove and add it the collection of moves to return
			  if(player.has(t.requiredTicket())){
				  moves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
			  }
			}


			// TODO consider the rules of secret moves here
			//  add moves to the destination via a secret ticket if there are any left with the player
			if(player.has(ScotlandYard.Ticket.SECRET)){
				moves.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));
			}
		  }

		  // TODO return the collection of moves
		  return  moves;
		}

		/**
		 * Implement Helper function: SingleMove
		 */
		private Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source){
			Set<Move.DoubleMove> moves = new HashSet<>();
			// Check if this is no Mr X
			if(!player.isMrX()){
			  throw new IllegalArgumentException("Only MrX can have Double tickets!");
			}

			// Check if whether player has Double Ticket or not and whether player has enough tickets to use Double move or not
			//Also check if player is in the last move
			if(player.tickets().getOrDefault(ScotlandYard.Ticket.DOUBLE, 0) < 1 || player.tickets().size() < 3 || this.log.size() + 2 > setup.moves.size()){
			  return moves;
			}

			//Initiate the all possible first Move for player
			Set<Move.SingleMove> firstMoves = (Set<Move.SingleMove>) makeSingleMoves(setup, detectives, player, source);

			//Loop for each first Move location find all possible location second move
			for(Move.SingleMove firstMove : firstMoves){
			  Set<Move.SingleMove> secondMoves = (Set<Move.SingleMove>) makeSingleMoves(setup, detectives, player, firstMove.destination);
			  for(Move.SingleMove secondMove : secondMoves){
				  moves.add(new Move.DoubleMove(player.piece(), player.location(), firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
			  }
			}
			return moves;
		}

		/**
		* @return the current available moves of the game.
		* This is mutually exclusive with {@link #getWinner()}
		*/
		@Override @Nonnull public ImmutableSet<Move> getAvailableMoves(){
			Set<Move> possibleMoves = new HashSet<>();
			if (!getWinner().isEmpty()) return ImmutableSet.copyOf(possibleMoves);
			Set<Move.SingleMove> singleMoves = new HashSet<>();
			Set<Move.DoubleMove> doubleMoves = new HashSet<>();
			for(Player player : detectives) {
				if (remaining.contains(player.piece())) {
					possibleMoves.addAll(makeSingleMoves(setup, detectives, player, player.location()));
				}
			}
			if(remaining.contains(mrX.piece())){
				possibleMoves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
				possibleMoves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
			}
			return ImmutableSet.copyOf(possibleMoves);
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
