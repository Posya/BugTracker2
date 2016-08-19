package site.kiselev.task;

import com.google.gson.annotations.SerializedName;

/**
 * Enum of tasks states
 */
public enum State {
    @SerializedName("active")   ACTIVE,
    @SerializedName("done")     DONE,
    @SerializedName("archived") ARCHIVED
}
