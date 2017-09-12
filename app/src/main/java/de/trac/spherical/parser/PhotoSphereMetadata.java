package de.trac.spherical.parser;

import java.util.Date;

/**
 * Created by vanitas on 12.09.17.
 */

public class PhotoSphereMetadata {

    public static final String USE_PANORAMA_VIEWER = "GPano:UsePanoramaViewer";
    public static final String CAPTURE_SOFTWARE = "GPano:CaptureSoftware";
    public static final String STITCHING_SOFTWARE = "GPano:StitchingSoftware";
    public static final String PROJECTION_TYPE = "GPano:ProjectionType";
    public static final String POSE_HEADING_DEGREES = "GPano:PoseHeadingDegrees";
    public static final String POSE_PITCH_DEGREES = "GPano:PosePitchDegrees";
    public static final String POSE_ROLL_DEGREES = "GPano:PoseRollDegrees";
    public static final String INITIAL_VIEW_HEADING_DEGREES = "GPano:InitialViewHeadingDegrees";
    public static final String INITIAL_VIEW_PITCH_DEGREES = "GPano:InitialViewPitchDegrees";
    public static final String INITIAL_VIEW_ROLL_DEGREES = "GPano:InitialViewRollDegrees";
    public static final String INITIAL_HORIZONTAL_POV_DEGREES = "GPano:InitialHorizontalFOVDegrees";
    public static final String FIRST_PHOTO_DATE = "GPano:FirstPhotoDate";
    public static final String LAST_PHOTO_DATE = "GPano:LastPhotoDate";
    public static final String SOURCE_PHOTOS_COUNT = "GPano:SourcePhotosCount";
    public static final String EXPOSURE_LOCK_USED = "GPano:ExposureLockUsed";
    public static final String CROPPED_AREA_IMAGE_WIDTH_PIXELS = "GPano:CroppedAreaImageWidthPixels";
    public static final String CROPPED_AREA_IMAGE_HEIGHT_PIXELS = "GPano:CroppedAreaImageHeightPixels";
    public static final String FULL_PANO_WIDTH_PIXELS = "GPano:FullPanoWidthPixels";
    public static final String FULL_PANO_HEIGHT_PIXELS = "GPano:FullPanoHeightPixels";
    public static final String CROPPED_AREA_LEFT_PIXELS = "GPano:CroppedAreaLeftPixels";
    public static final String CROPPED_AREA_TOP_PIXELS = "GPano:CroppedAreaTopPixels";
    public static final String INITIAL_CAMERA_DOLLY = "GPano:InitialCameraDolly";

    private static final String pf = "=\"";

    enum TYPE {
        equirectangular
    }

    private boolean usePanoramaViewer = true;
    private String captureSoftware = null;
    private String stitichingSoftware = null;
    private TYPE projectionType = TYPE.equirectangular;
    private Float poseHeadingDegrees = null;
    private float posePitchDegrees = 0;
    private float poseRollDegrees = 0;
    private int initialViewHeadingDegrees = 0;
    private int initialViewPitchDegrees = 0;
    private int initialViewRollDegrees = 0;
    private Float initialHorizontalFOVDegrees = null;
    private Date firstPhotoDate = null;
    private Date lastPhotoDate = null;
    private Integer sourcePhotosCount = null;
    private boolean exposureLockUsed = false;
    private Integer croppedAreaImageWidthPixels = null;
    private Integer croppedAreaImageHeightPixels = null;
    private Integer fullPanoWidthPixels = null;
    private Integer fullPanoHeightPixels = null;
    private Integer croppedAreaLeftPixels = null;
    private Integer croppedAreaTopPixels = null;
    private float initialCameraDolly = 0;

    public void setUsePanoramaViewer(boolean usePanoramaViewer) {
        this.usePanoramaViewer = usePanoramaViewer;
    }

    public void setCaptureSoftware(String captureSoftware) {
        this.captureSoftware = captureSoftware;
    }

    public void setStitichingSoftware(String stitichingSoftware) {
        this.stitichingSoftware = stitichingSoftware;
    }

    public void setProjectionType(TYPE projectionType) {
        this.projectionType = projectionType;
    }

    public void setPoseHeadingDegrees(Float poseHeadingDegrees) {
        this.poseHeadingDegrees = poseHeadingDegrees;
    }

    public void setPosePitchDegrees(float posePitchDegrees) {
        this.posePitchDegrees = posePitchDegrees;
    }

    public void setPoseRollDegrees(float poseRollDegrees) {
        this.poseRollDegrees = poseRollDegrees;
    }

    public void setInitialViewHeadingDegrees(int initialViewHeadingDegrees) {
        this.initialViewHeadingDegrees = initialViewHeadingDegrees;
    }

    public void setInitialViewPitchDegrees(int initialViewPitchDegrees) {
        this.initialViewPitchDegrees = initialViewPitchDegrees;
    }

    public void setInitialViewRollDegrees(int initialViewRollDegrees) {
        this.initialViewRollDegrees = initialViewRollDegrees;
    }

    public void setInitialHorizontalFOVDegrees(Float initialHorizontalFOVDegrees) {
        this.initialHorizontalFOVDegrees = initialHorizontalFOVDegrees;
    }

    public void setFirstPhotoDate(Date firstPhotoDate) {
        this.firstPhotoDate = firstPhotoDate;
    }

    public void setLastPhotoDate(Date lastPhotoDate) {
        this.lastPhotoDate = lastPhotoDate;
    }

    public void setSourcePhotosCount(Integer sourcePhotosCount) {
        this.sourcePhotosCount = sourcePhotosCount;
    }

    public void setExposureLockUsed(boolean exposureLockUsed) {
        this.exposureLockUsed = exposureLockUsed;
    }

    public void setCroppedAreaImageWidthPixels(Integer croppedAreaImageWidthPixels) {
        this.croppedAreaImageWidthPixels = croppedAreaImageWidthPixels;
    }

    public void setCroppedAreaImageHeightPixels(Integer croppedAreaImageHeightPixels) {
        this.croppedAreaImageHeightPixels = croppedAreaImageHeightPixels;
    }

    public void setFullPanoWidthPixels(Integer fullPanoWidthPixels) {
        this.fullPanoWidthPixels = fullPanoWidthPixels;
    }

    public void setFullPanoHeightPixels(Integer fullPanoHeightPixels) {
        this.fullPanoHeightPixels = fullPanoHeightPixels;
    }

    public void setCroppedAreaLeftPixels(Integer croppedAreaLeftPixels) {
        this.croppedAreaLeftPixels = croppedAreaLeftPixels;
    }

    public void setCroppedAreaTopPixels(Integer croppedAreaTopPixels) {
        this.croppedAreaTopPixels = croppedAreaTopPixels;
    }

    public void setInitialCameraDolly(float initialCameraDolly) {
        this.initialCameraDolly = initialCameraDolly;
    }

    public boolean isUsePanoramaViewer() {

        return usePanoramaViewer;
    }

    public String getCaptureSoftware() {
        return captureSoftware;
    }

    public String getStitichingSoftware() {
        return stitichingSoftware;
    }

    public TYPE getProjectionType() {
        return projectionType;
    }

    public Float getPoseHeadingDegrees() {
        return poseHeadingDegrees;
    }

    public float getPosePitchDegrees() {
        return posePitchDegrees;
    }

    public float getPoseRollDegrees() {
        return poseRollDegrees;
    }

    public int getInitialViewHeadingDegrees() {
        return initialViewHeadingDegrees;
    }

    public int getInitialViewPitchDegrees() {
        return initialViewPitchDegrees;
    }

    public int getInitialViewRollDegrees() {
        return initialViewRollDegrees;
    }

    public Float getInitialHorizontalFOVDegrees() {
        return initialHorizontalFOVDegrees;
    }

    public Date getFirstPhotoDate() {
        return firstPhotoDate;
    }

    public Date getLastPhotoDate() {
        return lastPhotoDate;
    }

    public Integer getSourcePhotosCount() {
        return sourcePhotosCount;
    }

    public boolean isExposureLockUsed() {
        return exposureLockUsed;
    }

    public Integer getCroppedAreaImageWidthPixels() {
        return croppedAreaImageWidthPixels;
    }

    public Integer getCroppedAreaImageHeightPixels() {
        return croppedAreaImageHeightPixels;
    }

    public Integer getFullPanoWidthPixels() {
        return fullPanoWidthPixels;
    }

    public Integer getFullPanoHeightPixels() {
        return fullPanoHeightPixels;
    }

    public Integer getCroppedAreaLeftPixels() {
        return croppedAreaLeftPixels;
    }

    public Integer getCroppedAreaTopPixels() {
        return croppedAreaTopPixels;
    }

    public float getInitialCameraDolly() {
        return initialCameraDolly;
    }
}
