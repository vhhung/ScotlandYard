package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.*;

import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;


/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	private final class MyGameState implements GameState {

		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;


		@Override public GameSetup getSetup() {
			return this.setup;
		}

		@Override public ImmutableSet<Piece> getPlayers() {
			List<Piece> allPieces = new ArrayList<>();
			allPieces.add(mrX.piece());
			for (Player detective : detectives) {
				allPieces.add(detective.piece());
			}

			return ImmutableSet.copyOf(allPieces);
		}

		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
//			return Optional.empty();
			return detectives.stream()
					.filter(d -> d.piece().equals(detective))
					.map(Player::location)
					.findFirst();
		}

		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			if(piece == mrX.piece())
			{
				return Optional.of(new TicketBoard() {
					@Override
					public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
						return mrX.tickets().getOrDefault(ticket, 0);
					}
				});
			}


			for (Player detective : detectives) {
				if (detective.piece().equals(piece)) {
					return Optional.of(ticket -> detective.tickets().getOrDefault(ticket, 0));
				}
			}



			return Optional.empty();
		}

		@Nonnull @Override
		public ImmutableList<LogEntry>  getMrXTravelLog() {
			return null;
		}

		@Nonnull @Override
		public ImmutableSet<Piece> getWinner() {
			return ImmutableSet.of();
		}




		private static boolean detectiveOccupied(int destination, List<Player> detectives) {
			if (detectives == null) return false;
			return detectives.stream().anyMatch(detective -> detective.location() == destination);
		}

		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

			// TODO create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
			Set<Move.SingleMove> moves = new HashSet<>();
			for(int destination : setup.graph.adjacentNodes(source)) {
				// TODO find out if destination is occupied by a detective
				//  if the location is occupied, don't add to the collection of moves to return
				// if(ScotlandYard.DETECTIVE_LOCATIONS.contains(destination)) {}
				if (detectiveOccupied(destination, detectives)) continue;

				for(ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
					// TODO find out if the player has the required tickets
					//  if it does, construct a SingleMove and add it the collection of moves to return
					if (player.has(t.requiredTicket())) {
						moves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
					}
				}

				// TODO consider the rules of secret moves here
				//  add moves to the destination via a secret ticket if there are any left with the player
				if (player.has(ScotlandYard.Ticket.SECRET)) {
					moves.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));
				}
			}

			// TODO return the collection of moves
			return moves;
		}






		@Nonnull @Override
		public ImmutableSet<Move> getAvailableMoves() {
			//return null;
//			Set<Move.SingleMove> moves = makeSingleMoves(setup, detectives, currentPlayer, currentPlayer.location());
//			return ImmutableSet.of();

//			Player player = getCurrentPlayer(); // Replace with actual method or logic
//			if (player == null) return ImmutableSet.of(); // Prevent NullPointerException
//
//			Set<Move.SingleMove> moves = makeSingleMoves(setup, detectives, player, player.location());
			//return ImmutableSet.copyOf(moves);

//			ImmutableSet.Builder<Move> moves = ImmutableSet.builder();
//
//			if (remainingMoves.isEmpty()) return ImmutableSet.of(); // No moves available
//
//			Piece currentPlayer = remainingMoves.get(0); // First player in the queue (it's their turn)
//
//			// Check if the current player is Mr. X
//			if (currentPlayer.equals(mrX.piece())) {
//				moves.addAll(generateMoves(mrX));
//			} else {
//				// The current player is a detective, find the corresponding detective player
//				for (Player detective : detectives) {
//					if (detective.piece().equals(currentPlayer)) {
//						moves.addAll(generateMoves(detective));
//						break;
//					}
//				}
//			}
//
//			return moves.build();

			//if (isGameOver()) return ImmutableSet.of();

			ImmutableSet<Piece> currentPlayer = getPlayers();; // Step 2: Get the current player

			// Step 3: Generate available moves based on player type
			if (currentPlayer.piece().isMrX()) {
				return ImmutableSet.copyOf(makeMrXMoves(setup, detectives, currentPlayer));
			} else {
				return ImmutableSet.copyOf(makeSingleMoves(setup, detectives, currentPlayer, currentPlayer.location()));
			}

		}

		@Override
		public GameState advance(Move move) {
			return move.accept(new Move.Visitor<GameState>() {
				@Override
				public GameState visit(Move.SingleMove move) {
					move.commencedBy();
					return null;
				}

				@Override public GameState visit(Move.DoubleMove move) {
					return null;
				}
			});
		}



		// @Override public GameSetup getSetup() {  return null; }
		// @Override public ImmutableSet<Piece> getPlayers() { return null; }
		// @Override public GameState advance(Move move) {  return null;  }

		private MyGameState(final GameSetup setup,
							final ImmutableSet<Piece> remaining,
							final ImmutableList<LogEntry> log,
							final Player mrX,
							final List<Player> detectives) {


			// if (remaining == null) throw new NullPointerException("Remaining pieces cannot be null");
			// if (log == null) throw new NullPointerException("Log entries cannot be null");


			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;


		}
	}

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		// throw new RuntimeException("Implement me!");

		if (mrX == null) throw new NullPointerException("MrX cannot be null"); // Correct!!
		//if (detectives == null) throw new NullPointerException("Detectives list cannot be null");  // not about the test
		if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!"); // Correct!!
		if (setup.graph.nodes().isEmpty()) // Correct!!
			throw new IllegalArgumentException("Graph cannot be empty");

		// NoDoubleMove
		for (Player detective : detectives) { // Correct!!
			if (detective.tickets().getOrDefault(ScotlandYard.Ticket.DOUBLE, 0) > 0) {
				throw new IllegalArgumentException("No double tickets for detectives.");
			}
		}

		// NoSecret
		for (Player detective : detectives) {
			if (detective.tickets().getOrDefault(ScotlandYard.Ticket.SECRET, 0) > 0) {
				throw new IllegalArgumentException("No secret tickets for detectives.");
			}
		}


		// DetectivesSameLocation
		Set<Integer> detectiveLocations = new HashSet<>(); //Correct!!
		for (Player detective : detectives) { //Correct!!
			if (!detectiveLocations.add(detective.location())) {
				throw new IllegalArgumentException("Don't Start at the same location.");
			}
		}

		return new MyGameState(setup, ImmutableSet.of(mrX.piece()), ImmutableList.of(), mrX, detectives);


	}

}
