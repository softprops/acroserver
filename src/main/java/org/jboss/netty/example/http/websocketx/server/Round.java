package org.jboss.netty.example.http.websocketx.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class Round {

	@Expose
	private String category;

	@Expose
	private String acronym;

	private Map<String, Acronym> acronyms = new HashMap<String, Acronym>();

	private static Comparator<Acronym> comparator = new Comparator<Acronym>() {
		@Override
		public int compare(Acronym arg0, Acronym arg1) {
			return Integer.valueOf(arg0.votes.size()).compareTo(
					arg1.votes.size());
		}
	};

	public Acronym getWinner() {
		List<Acronym> acros = new ArrayList<Acronym>(acronyms.size());
		acros.addAll(acronyms.values());
		Collections.sort(acros, comparator);
		Acronym prev = acros.get(0);
		for (int i = 1; i < acros.size(); i++) {
			Acronym acro = acros.get(i);
			if (acro.votes.size() == prev.votes.size()) {
				if (acro.received < prev.received) {
					prev = acro;
				}
			}
		}
		return prev;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	
	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	public Map<String, Acronym> getAcronyms() {
		return acronyms;
	}

	public void setAcronyms(Map<String, Acronym> acronyms) {
		this.acronyms = acronyms;
	}

}
