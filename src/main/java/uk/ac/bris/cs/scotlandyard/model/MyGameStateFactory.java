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
		private MyGameState(final GameSetup setup,
							final ImmutableSet<Piece> remaining,
							final ImmutableList<LogEntry> log,
							final Player mrX,
							final List<Player> detectives){
			//Check if the moves is empty
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");

			//Check if the graph is empty
			if(setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Nodes is empty!");


			//Check if the color of mrX is not black
			if(!mrX.piece().webColour().equals("#000")) throw new IllegalArgumentException("MrX is not a black piece!");

			//Check if the detectives is empty
			if(detectives.isEmpty()) throw new IllegalArgumentException("Detectives is empty!");

			// Check is there any duplicated location
			// Check is there any detective having Double ticket or Secret ticket
			// Check if there is mrX in detectives list
			List<Integer> locations = new ArrayList<>();
			for(Player detective : detectives){
				int location = detective.location();
				if(locations.contains(location)) throw new IllegalArgumentException("Detectives cannot have duplicate locations!");
				locations.add(location);
				if(detective.hasAtLeast(ScotlandYard.Ticket.SECRET, 1)){
					throw new IllegalArgumentException("Detectives cannot have Secret tickets!");
				}
				if(detective.hasAtLeast(ScotlandYard.Ticket.DOUBLE, 1)){
					throw new IllegalArgumentException("Detectives cannot have Double tickets!");
				}
				if(detective.equals(mrX)) throw new IllegalArgumentException("Detectives cannot have MrX!");
			}

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			this.moves = getAvailableMoves();
			this.winner = getWinner();
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
			List<Piece> players = new ArrayList<>();
			players.add(mrX.piece());
			players.addAll(detectives.stream()
									 .map(Player::piece)
									 .toList());
			return ImmutableSet.copyOf(players);
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
		 * Helper function to check whether all detectives have run out of move
		 */
		private boolean movesOfDetectives(List<Player> detectives){
			return detectives.stream()
					         .allMatch(detective -> makeSingleMoves(setup, detectives, detective, detective.location()).isEmpty());
		}

		/**
		 * Helper function to check whether Mr X has run out of move
		 */
		private boolean movesOfMrX(){
			return (makeSingleMoves(setup, detectives, mrX, mrX.location()).isEmpty()
				   && remaining.contains(mrX.piece()));
		}

		/**
		* @return the winner of this game; empty if the game has no winners yet
		* This is mutually exclusive with {@link #getAvailableMoves()}
		*/
		@Override @Nonnull public ImmutableSet<Piece> getWinner(){
			// Create a list of detectives' pieces
			List<Piece> detectivePiecesList = detectives.stream()
														.map(Player::piece)
														.toList();


			// If a detective is on the same location with Mr X, then Mr X loses
            for (Player detective : detectives) {
                if (detective.location() == this.mrX.location()) {
                    return ImmutableSet.copyOf(detectivePiecesList);
                }
            }

			// If no detectives have any available move left to go then Mr X win
			if (movesOfDetectives(detectives)) {
				return ImmutableSet.of(Piece.MrX.MRX);
			}

            // if it's turn for mrX to move but there are no available moves then game over, Mr X loses
            if (movesOfMrX()) {
                return ImmutableSet.copyOf(detectivePiecesList);
            }


            // mrx manage to fill up the log and no detectives could catch him, then Mr X win
            if (setup.moves.size() == getMrXTravelLog().size()
                    && remaining.contains(mrX.piece())) {
                return ImmutableSet.of(Piece.MrX.MRX);
            }

			// If no one win then return Immutable Set of empty
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
			if (detectives.stream().anyMatch(detective -> detective.location() == destination)) continue;

			for(ScotlandYard.Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
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
		 * Implement Helper function: DoubleMove
		 */
		private static Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source, ImmutableList<LogEntry> log){
			Set<Move.DoubleMove> moves = new HashSet<>();

			// Check if this is not Mr X
			if(!player.isMrX()){
			  return moves;
			}

			// Check if whether player has Double Ticket or not
			//Also check if player have enough moves to use Double move
			if(!player.hasAtLeast(ScotlandYard.Ticket.DOUBLE, 1) || log.size() + 2 > setup.moves.size() ){
			  return moves;
			}


			//Find all possible first moves for player
			Set<Move.SingleMove> firstMoves = makeSingleMoves(setup, detectives, player, source);

			//Loop for each first Move location find all possible location for the second move
			for(Move.SingleMove firstMove : firstMoves){
			  Set<Move.SingleMove> secondMoves = makeSingleMoves(setup, detectives, player, firstMove.destination);

				//If MrX can do the second move then add that double move to moves
				if(!secondMoves.isEmpty()) {
					for (Move.SingleMove secondMove : secondMoves) {
						if (firstMove.ticket == secondMove.ticket && player.hasAtLeast(firstMove.ticket, 2)) {
							moves.add(new Move.DoubleMove(player.piece(), player.location(), firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
						} else if (firstMove.ticket != secondMove.ticket) {
							moves.add(new Move.DoubleMove(player.piece(), player.location(), firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
						}
					}
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

			if(remaining.contains(mrX.piece())){
				singleMoves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
				doubleMoves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location(), log));
			} else {
				for (Player player : detectives) {
					if (remaining.contains(player.piece())) {
						singleMoves.addAll(makeSingleMoves(setup, detectives, player, player.location()));
					}
				}
			}
			if(!singleMoves.isEmpty()){
				possibleMoves.addAll(singleMoves);
			}
			if(!doubleMoves.isEmpty()){
				possibleMoves.addAll(doubleMoves);
			}
			// possibleMoves.addAll(doubleMoves);
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
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
			return move.accept(new Move.Visitor<>(){
				@Override
				public GameState visit(Move.SingleMove singleMove) {
					if(singleMove.commencedBy().isMrX()){
						mrX = mrX.use(singleMove.ticket).at(singleMove.destination);
						//Update the log
						log = ImmutableList.<LogEntry>builder()
										   .addAll(log)
										   .add(setup.moves.get(log.size())
												   ? LogEntry.reveal(singleMove.ticket, singleMove.destination)
												   : LogEntry.hidden(singleMove.ticket))
										   .build();

						// After Mr X did the move then it's detectives turn so all detectives' pieces need to be added to the remaining
						remaining = ImmutableSet.copyOf(detectives.stream()
																  .filter(detective -> !makeSingleMoves(setup, detectives, detective, detective.location()).isEmpty())
																  .map(Player::piece)
																  .toList());

					} else {
						List<Player> updateDetectives = new ArrayList<>();
						for(Player detective : detectives){
							if(detective.piece().equals(singleMove.commencedBy())){
								detective = detective.use(singleMove.ticket).at(singleMove.destination);
							}
							updateDetectives.add(detective);
						}
						detectives = updateDetectives;
						mrX = mrX.give(singleMove.ticket);
							// Update the remaining by add all detective except for the one who just done the move
						    remaining = ImmutableSet.copyOf(remaining.stream()
												    .filter(piece -> !piece.equals(move.commencedBy()))
												    .toList());
							// If there is no more detective left in the remaining then add Mr X piece to begin new turn
							if (remaining.isEmpty()){
								remaining = ImmutableSet.of(Piece.MrX.MRX);
							}
					}
					return new MyGameState(setup, remaining, log, mrX, detectives);
				}

				@Override
				public GameState visit(Move.DoubleMove doubleMove) {
					mrX = mrX.use(doubleMove.tickets()).at(doubleMove.destination2);
					//Update the log after first move of Mr X
					log = ImmutableList.<LogEntry>builder()
									   .addAll(log)
									   .add(setup.moves.get(log.size())
										   ? LogEntry.reveal(doubleMove.ticket1, doubleMove.destination1)
										   : LogEntry.hidden(doubleMove.ticket1))
									   .build();
					//Update the log after second move of Mr X
					log = ImmutableList.<LogEntry>builder()
									   .addAll(log)
									   .add(setup.moves.get(log.size())
										   ? LogEntry.reveal(doubleMove.ticket2, doubleMove.destination2)
										   : LogEntry.hidden(doubleMove.ticket2))
									   .build();
					//Add all the detectives' pieces to begin the detectives' turns
					remaining = ImmutableSet.copyOf(detectives.stream()
															  .map(Player::piece)
															  .toList());
					return new MyGameState(setup, remaining, log, mrX, detectives);
				}
			});

		}
	}
}