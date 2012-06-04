package org.jboss.netty.example.http.websocketx.server;

import java.util.Collection;
import com.google.gson.annotations.Expose;

public class Answers {
	
	public Answers() {}
	public Answers(Collection<Acronym> answers, Acronym winner, Acronym speeder) {
		this.answers = answers;
		if(winner!=null) {
			this.winner = winner.getPlayer().getUserId();
		}
		if(speeder!=null) {
			this.speeder = speeder.getPlayer().getUserId();
		}
	}
	
	@Expose
	private Collection<Acronym> answers;
	
	@Expose
	private String winner;
	
	@Expose
	private String speeder;
	
	public String getSpeeder() {
		return speeder;
	}
	
	public String getWinner() {
		return winner;
	}
	
	public Collection<Acronym> getAnswers() {
		return answers;
	}
	
}