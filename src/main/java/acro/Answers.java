package acro;

import java.util.Collection;
import java.util.List;
import com.google.gson.annotations.Expose;

public class Answers {
	
	public Answers() {}
	public Answers(Collection<Acronym> answers, Acronym winner, Acronym speeder, List<String> winnerBonuses) {
		this.answers = answers;
		if(winner!=null) {
			this.winner = winner.getPlayer().getUserId();
		}
		if(speeder!=null) {
			this.speeder = speeder.getPlayer().getUserId();
		}
		this.winnerBonuses = winnerBonuses;
	}
	
	@Expose
	private Collection<Acronym> answers;
	
	@Expose
	private String winner;
	
	@Expose
	private String speeder;
	
	@Expose
	List<String> winnerBonuses;
	
	public String getSpeeder() {
		return speeder;
	}
	
	public String getWinner() {
		return winner;
	}
	
	public Collection<Acronym> getAnswers() {
		return answers;
	}
	
	public List<String> getWinnerBonuses() {
		return winnerBonuses;
	}
	
}