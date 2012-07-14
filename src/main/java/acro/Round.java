package acro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import com.google.gson.annotations.Expose;

public class Round {

	@Expose
	private String category;

	@Expose
	private String acronym;

	@Expose private int round;

	private Map<String, Acronym> acronyms = new HashMap<String, Acronym>();

	private static Comparator<Acronym> comparator = new Comparator<Acronym>() {
		@Override
		public int compare(Acronym arg0, Acronym arg1) {
			return Integer.valueOf(arg0.votes.size()).compareTo(
					arg1.votes.size());
		}
	};
	
	private List<Acronym> __acroz;
	
	private List<Acronym> getAcroz() {
		// if(__acroz!=null) {
		// 	return __acroz;
		// }
		System.out.println("getAcroz");
		List<Acronym> acros = new ArrayList<Acronym>(acronyms.size());
		System.out.println("acro size" + acros.size());
		acros.addAll(acronyms.values());
		Map<String,Integer> voted = new HashMap<String,Integer>();
		for(Acronym acro : acros) {
			for(String voter:acro.votes) {
				Integer count = voted.get(voter);
				if(count==null) {
					System.out.println("found " + voter);
					count = 1;
				} else {
					System.out.println("cheater " + voter);
					count++;
				}
				voted.put(voter,count);
			}
		}
		Iterator<Acronym> i = acros.iterator();
		while(i.hasNext()) {
			Acronym acro = i.next();
			Integer count = voted.get(acro.getPlayer().getUserId());
			System.out.println("coutn is " + count);
			if(count==null || count==0) {
				i.remove();
			}
			
		}
		Collections.sort(acros, comparator);
		__acroz = acros;
		return __acroz;
	}

	public Acronym getWinner() {
		List<Acronym> acros = getAcroz();
		if(acros.isEmpty()) {
			return null;
		}
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

	public List<String> getWinnerBonuses() {
		List<Acronym> acros = getAcroz();
		if(acros.isEmpty()) {
			return Collections.emptyList();
		}
		Acronym winner = getWinner();
		List<String> winnars = new ArrayList<String>();
		if(winner!=null) {
			for(Acronym acro : acros) {
				if(winner.votes.contains(acro.getPlayer().getUserId())) {
					winnars.add(acro.getPlayer().getUserId());
				}
			}		
		}
		return winnars;
	}

	public Acronym getSpeeder() {
		List<Acronym> acros = getAcroz();
		if(acros.isEmpty()) {
			return null;
		}
		Acronym fastest = null;
		for(Acronym acro : acros) {
			if(acro.votes.isEmpty()) {
				continue;
			}
			if(acro.getPlayer().getTotalVoteCount()>26) {
				continue;
			}
			if(fastest!=null) {
				if(fastest.received<=acro.received) {
					fastest = acro;
				}
			} else {
				fastest = acro;
			}
		}
		return fastest;
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

	public int getRound() {
		return round;
	}
	public void setRound(int round) {
		this.round = round;
	}

	public Map<String, Acronym> getAcronyms() {
		return acronyms;
	}

	public void setAcronyms(Map<String, Acronym> acronyms) {
		this.acronyms = acronyms;
	}

	public void addAnswer(String userId, Acronym ans) {
		acronyms.put(userId, ans);
	}

	public Acronym getAnswer(String userId) {
		return acronyms.get(userId);
	}

	public void addVote(String voterId, String answerUserId) {
		for (Acronym acronym : acronyms.values())
			acronym.removeVote(voterId);
		if (acronyms.containsKey(answerUserId))
			acronyms.get(answerUserId).addVote(voterId);
	}

	public Answers getAnswers() {
		return new Answers(acronyms.values(),getWinner(),getSpeeder(),getWinnerBonuses());
	}
}
