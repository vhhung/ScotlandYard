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

		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
//			return Optional.empty();
			return detectives.stream()
					.filter(d -> d.piece().equals(detective))
					.map(Player::location)
					.findFirst();
		}

		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			if(piece.isMrX())
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
			return log;
		}

		@Nonnull @Override
		public ImmutableSet<Piece> getWinner() {
			return ImmutableSet.of();
		}




		private static boolean detectiveOccupied(int destination, List<Player> detectives) {
			//if (detectives == null) return false;
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


		// Max's
//		private static Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
//			Set<Move.DoubleMove> doubleMoves = new HashSet<>();
//
//			// Mr. X must have a DOUBLE ticket
//			if (!player.has(ScotlandYard.Ticket.DOUBLE)) return doubleMoves;
//
//			// Generate first moves
//			Set<Move.SingleMove> firstMoves = makeSingleMoves(setup, detectives, player, source);
//
//			for (Move.SingleMove firstMove : firstMoves) {
//				int firstDestination = firstMove.destination;
//
//				Set<Move.SingleMove> secondMoves = makeSingleMoves(setup, detectives, player, firstDestination);
//
//				for (Move.SingleMove secondMove : secondMoves) {
//					int Destination2 = secondMove.destination;
//
//					// Create and add a DoubleMove
//					doubleMoves.add(new Move.DoubleMove(player.piece(),
//							source, firstMove.ticket, firstDestination,
//							ScotlandYard.Ticket.DOUBLE, Destination2));
//				}
//			}
//			return doubleMoves;
//		}


		// Hung's
		private Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source){
			Set<Move.DoubleMove> moves = new HashSet<>();
			// Check if this is no Mr X
			if(!player.isMrX()){
				//throw new IllegalArgumentException("Only MrX can have Double tickets!");
				return moves;
			}

			// Check if whether player has Double Ticket or not and whether player has enough tickets to use Double move or not
			//Also check if player is in the last move

			if(player.tickets().getOrDefault(ScotlandYard.Ticket.DOUBLE, 0) < 1 || this.log.size() + 2 > setup.moves.size()){
				return moves;
			}

			//Initiate the all possible first Move for player
			Set<Move.SingleMove> firstMoves = makeSingleMoves(setup, detectives, player, source);

			//Loop for each first Move location find all possible location second move
			for(Move.SingleMove firstMove : firstMoves){
				Set<Move.SingleMove> secondMoves = makeSingleMoves(setup, detectives, player, firstMove.destination);
				for(Move.SingleMove secondMove : secondMoves){
					if(firstMove.ticket == secondMove.ticket && player.hasAtLeast(firstMove.ticket, 2)){
						moves.add(new Move.DoubleMove(player.piece(), player.location(), firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
					} else if(firstMove.ticket != secondMove.ticket){
						moves.add(new Move.DoubleMove(player.piece(), player.location(), firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
					}
				}
			}
            return moves;
        }



		@Override @Nonnull public ImmutableSet<Move> getAvailableMoves() {
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



		@Override
		public GameState advance(Move move) {
			if (!getAvailableMoves().contains(move)) {
				throw new IllegalArgumentException("Move not allowed: " + move); // Correct!!
			}
			// Teacher Suggest
			// if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			return move.accept(new Move.Visitor<GameState>() {
				@Override
				public GameState visit(Move.SingleMove move) {
					Piece piece = move.commencedBy();

					Player player;
					if (piece.isMrX()) {
						player = mrX;
					} else {
						player = null;
						for (Player p : detectives) {
							if (p.piece().equals(piece)) {
								player = p;
								break;
							}
						}
					}

					Player updatedPlayer = player.use(move.tickets());

					List<Player> updatedDetectives = new ArrayList<>(detectives);
					if (!piece.isMrX()) {
						updatedDetectives.remove(player);
						updatedDetectives.add(updatedPlayer);
					}

					Player updatedMrX = piece.isMrX() ? updatedPlayer : mrX;

					List<LogEntry> updatedLog = new ArrayList<>(log);
					if (piece.isMrX()) {
						if (player.has(ScotlandYard.Ticket.SECRET)) {
							updatedLog.add(LogEntry.hidden(move.ticket));
						} else {
							updatedLog.add(LogEntry.reveal(move.ticket, move.destination));
						}
					}

					ImmutableSet<Piece> updatedRemaining;
					if (remaining.size() == 1) {
						updatedRemaining = ImmutableSet.copyOf(getPlayers()); // Reset turn order
					} else {
						updatedRemaining = ImmutableSet.copyOf(remaining.stream().filter(p -> !p.equals(piece)).toList());
					}

					//return null;
					return new MyGameState(setup, updatedRemaining, ImmutableList.copyOf(updatedLog), updatedMrX, updatedDetectives);

				}





				@Override public GameState visit(Move.DoubleMove move) {
					//travel log
					// Ensure the move is valid
					if (!getAvailableMoves().contains(move)) {
						throw new IllegalArgumentException("Move not allowed: " + move);
					}

					// Mr. X uses two tickets
					mrX.use(move.ticket1);
					mrX.use(move.ticket2);

					// Update travel log (consider secret moves)
					ImmutableList.Builder<LogEntry> newLog = ImmutableList.builder();
					newLog.addAll(log); // Copy previous log

					if (move.ticket1 == ScotlandYard.Ticket.SECRET) {
						newLog.add(LogEntry.hidden(move.ticket1));
					} else {
						newLog.add(LogEntry.reveal(move.ticket1, move.destination1));
					}

					if (move.ticket2 == ScotlandYard.Ticket.SECRET) {
						newLog.add(LogEntry.hidden(move.ticket2));
					} else {
						newLog.add(LogEntry.reveal(move.ticket2, move.destination2));
					}

					// Update Mr. X's position
					Player newMrX = new Player(mrX.piece(), mrX.tickets(), move.destination2);

					// Determine the next remaining players
					ImmutableSet<Piece> newRemaining;
					if (remaining.equals(ImmutableSet.of(mrX))) {
						// If Mr. X just played, reset to all detectives
						newRemaining = detectives.stream().map(Player::piece).collect(ImmutableSet.toImmutableSet());
					} else {
						// Otherwise, move to the next detective
						List<Piece> detectivePieces = detectives.stream().map(Player::piece).toList();
						int nextIndex = (detectivePieces.indexOf(remaining.iterator().next()) + 1) % detectivePieces.size();
						newRemaining = ImmutableSet.of(detectivePieces.get(nextIndex));
					}
//					ImmutableSet<Piece> newRemaining = detectives.stream()
//							.map(Player::piece)
//							.collect(ImmutableSet.toImmutableSet()); // All detectives should be next


					// Return new game state
					return new MyGameState(setup, newRemaining, newLog.build(), newMrX, detectives);
					//return null;
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
