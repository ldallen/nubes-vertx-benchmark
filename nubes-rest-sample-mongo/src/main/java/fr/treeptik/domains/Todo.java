package fr.treeptik.domains;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Todo{


	private Integer id=null;
	private String action;
	private Boolean done;

	public Todo() {
	}

	public Todo(String action, Boolean done) {
		super();
		this.action = action;
		this.done = done;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Boolean getDone() {
		return done;
	}

	public void setDone(Boolean done) {
		this.done = done;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Todo [id=" + id + ", action=" + action + ", done=" + done + "]";
	}



}
