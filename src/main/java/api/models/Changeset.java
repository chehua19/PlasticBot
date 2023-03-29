package api.models;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.Locale;

public class Changeset {
    private String comment;
    private Date creationDate;
    private Branch branch;
    private Owner owner;
    private Repository repository;

    public Repository getRepository() {
        return repository;
    }

    public Owner getOwner() {
        return owner;
    }

    public Branch getBranch() {
        return branch;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getComment() {
        return comment;
    }

    public String toString(){
        DateTimeFormatter sourceDateFormat = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        LocalDateTime sourceTime = LocalDateTime.parse(this.creationDate.toString(), sourceDateFormat);

        ZonedDateTime zdt = ZonedDateTime.of(sourceTime, ZoneId.of("+0"));
        ZonedDateTime currentISTime = zdt.withZoneSameInstant(ZoneId.of("+3"));

        return String.format("Repository: %s\nOwner: %s\nBranch: %s\nComment: %s\nCreation Time: %s",
                this.repository.getName(),
                this.owner.getName(),
                this.branch.getName(),
                this.comment,
                currentISTime.format(DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy"))
        );
    }
}
