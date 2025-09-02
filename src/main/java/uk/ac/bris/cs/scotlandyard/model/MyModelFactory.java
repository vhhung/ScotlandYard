package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		// TODO
		return new Model() {
			private Board.GameState gameState = new MyGameStateFactory().build(setup, mrX, detectives);
			private List<Observer> observers = new ArrayList<>();
			@Nonnull
			@Override
			public Board getCurrentBoard() {
				return gameState;
			}

			@Override
			public void registerObserver(@Nonnull Observer observer) {
				// Check whether the obeserver is null
				if (observer.equals(null)) throw new NullPointerException("The observer is Null!");
				// Check if there is an observer in the observers list match with the observer which is parsed
                if (observers.stream()
							 .anyMatch(o -> o.equals(observer))) {
					throw new IllegalArgumentException("The observer is already registered!");
				}
				observers.add(observer);
			}

			@Override
			public void unregisterObserver(@Nonnull Observer observer) {
				// Check whether the observer which is parsed is null
				if (observer.equals(null)) throw new NullPointerException("The observer is Null!");
				// Check not any observer in the observers list match with the observer which is parsed
                if (observers.stream()
							 .noneMatch(o -> o.equals(observer))) {
					throw new IllegalArgumentException("The observer hasn't registered!");
				}
				observers.remove(observer);
			}

			@Nonnull
			@Override
			public ImmutableSet<Observer> getObservers() {
				return ImmutableSet.copyOf(observers);
			}

			@Override
			public void chooseMove(@Nonnull Move move) {
				gameState = gameState.advance(move);
				// Notify the game state to all observers
				for(Observer observer : observers) {
					if (!gameState.getWinner().isEmpty()) {
						observer.onModelChanged(gameState, Observer.Event.GAME_OVER);
					} else {
						observer.onModelChanged(gameState, Observer.Event.MOVE_MADE);
					}
				}
			}
		};
	}
}
