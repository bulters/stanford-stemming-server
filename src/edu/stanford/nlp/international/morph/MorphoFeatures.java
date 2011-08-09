package edu.stanford.nlp.international.morph;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.international.morph.MorphoFeatureSpecification.MorphoFeatureType;

public class MorphoFeatures implements Serializable {
  
  private static final long serialVersionUID = -3893316324305154940L;

  protected final Map<MorphoFeatureType,String> fSpec;
  
  public MorphoFeatures() {
    fSpec = new HashMap<MorphoFeatureType,String>();
  }
  
  public void addFeature(MorphoFeatureType feat, String val) {
    fSpec.put(feat, val);
  }
  
  public boolean hasFeature(MorphoFeatureType feat) {
    return fSpec.containsKey(feat);
  }
  
  public String getValue(MorphoFeatureType feat) {
    if(fSpec.containsKey(feat))
      return fSpec.get(feat);
    throw new IllegalArgumentException("Value requested for non-existent feature: " + feat.toString());
  }
  
  public int numFeatureMatches(MorphoFeatures other) {
    int nMatches = 0;
    for(Map.Entry<MorphoFeatureType, String> fPair : fSpec.entrySet()) {
      if(other.hasFeature(fPair.getKey()) && other.getValue(fPair.getKey()).equals(fPair.getValue()))
        nMatches++;
    }
    
    return nMatches;
  }
  
  public int numActiveFeatures() { return fSpec.keySet().size(); }
  
  /**
   * Build a POS tag consisting of a base category plus inflectional features.
   * 
   * @param baseTag
   * @return
   */
  public String getTag(String baseTag) {
    return baseTag + toString();
  }
  
  /**
   * Assumes that the tag string has been formed using a call to getTag(). As such,
   * it removes the basic category from the feature string.
   * 
   * @param str
   * @return
   */
  public MorphoFeatures fromTagString(String str) {
    List<String> feats = Arrays.asList(str.split("\\-"));
    if(feats.size() > 0)
      feats.remove(0);//Remove the base tag
    MorphoFeatures mFeats = new MorphoFeatures();
    for(String fPair : feats) {
      String[] keyValue = fPair.split(":");
      if(keyValue.length != 2)
        throw new RuntimeException("Malformed key/value pair: " + fPair);
      MorphoFeatureType fName = MorphoFeatureType.valueOf(keyValue[0].trim());
      fSpec.put(fName, keyValue[1].trim());
    }
    return mFeats;
  }
  
  /**
   * values() returns the values in the order in which they are declared. Thus we will not have
   * the case where two feature types can yield two strings:
   *    -feat1:A-feat2:B
   *    -feat2:B-feat1:A
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(MorphoFeatureType feat : MorphoFeatureType.values())
      if(fSpec.containsKey(feat))
        sb.append(String.format("-%s:%s",feat.toString(),fSpec.get(feat)));
    
    return sb.toString();
  }
}
