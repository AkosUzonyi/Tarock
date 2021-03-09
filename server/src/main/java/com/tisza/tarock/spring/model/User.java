package com.tisza.tarock.spring.model;

import com.tisza.tarock.server.database.*;
import com.tisza.tarock.server.player.*;
import io.reactivex.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class User
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int id;
	public String name;
	public String img_url;
	public long registrationTime;
}
