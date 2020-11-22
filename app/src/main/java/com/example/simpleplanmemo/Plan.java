package com.example.simpleplanmemo;

import java.io.Serializable;

public class Plan implements Serializable {
    String id;
    String title;
    String contents;
    String createDate;
    String deadlineDate;
    String status;

    public Plan(String id, String title, String contents, String createDate, String deadlineDate, String status){
        this.id = id;
        this.title = title;
        if(contents==null){
            this.contents = "未登録";
        }else{
            this.contents = contents;
        }
        this.createDate = createDate;
        if(deadlineDate == null){
            this.deadlineDate = "未登録";
        }else{
            this.deadlineDate = deadlineDate;
        }
        this.status = status;
    }

    public String getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public String getContents(){
        return contents;
    }

    public String getCreateDate(){
        return createDate;
    }

    public String getDeadlineDate(){
        return deadlineDate;
    }

    public String getStatus(){
        return status;
    }

}
