package aaditya.myruns5;

import java.util.Date;

public class ExerciseEntry
{
  private int activityType = -1;
  private double avgPace = 0.0D;
  private double avgSpeed = 0.0D;
  private int calorie = 0;
  private double climb = 0.0D;
  private String comment = "";
  private Date dateTime = new Date(System.currentTimeMillis());
  private double distance = 0.0D;
  private int duration = 0;
  private int heartrate = 0;
  private Long id;
  private int inputType = -1;
  private long remoteId = -1L;

  public int getActivityType()
  {
    return this.activityType;
  }

  public double getAvgPace()
  {
    return this.avgPace;
  }

  public double getAvgSpeed()
  {
    return this.avgSpeed;
  }

  public int getCalorie()
  {
    return this.calorie;
  }

  public double getClimb()
  {
    return this.climb;
  }

  public String getComment()
  {
    return this.comment;
  }

  public Date getDateTime()
  {
    return this.dateTime;
  }

  public double getDistance()
  {
    return this.distance;
  }

  public int getDuration()
  {
    return this.duration;
  }

  public int getHeartrate()
  {
    return this.heartrate;
  }

  public Long getId()
  {
    return this.id;
  }

  public int getInputType()
  {
    return this.inputType;
  }

  public long getRemoteId()
  {
    return this.remoteId;
  }

  public void setActivityType(int paramInt)
  {
    this.activityType = paramInt;
  }

  public void setAvgPace(double paramDouble)
  {
    this.avgPace = paramDouble;
  }

  public void setAvgSpeed(double paramDouble)
  {
    this.avgSpeed = paramDouble;
  }

  public void setCalorie(int paramInt)
  {
    this.calorie = paramInt;
  }

  public void setClimb(double paramDouble)
  {
    this.climb = paramDouble;
  }

  public void setComment(String paramString)
  {
    this.comment = paramString;
  }

  public void setDateTime(Date paramDate)
  {
    this.dateTime = paramDate;
  }

  public void setDistance(double paramDouble)
  {
    this.distance = paramDouble;
  }

  public void setDuration(int paramInt)
  {
    this.duration = paramInt;
  }

  public void setHeartrate(int paramInt)
  {
    this.heartrate = paramInt;
  }

  public void setId(Long paramLong)
  {
    this.id = paramLong;
  }

  public void setInputType(int paramInt)
  {
    this.inputType = paramInt;
  }

  public void setRemoteId(long paramLong)
  {
    this.remoteId = paramLong;
  }

  public String toString()
  {
    return super.toString();
  }
}

