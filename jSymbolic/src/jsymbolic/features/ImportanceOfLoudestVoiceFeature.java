/*
 * ImportanceOfLoudestVoiceFeature.java
 * Version 1.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jsymbolic.features;

import java.util.LinkedList;
import javax.sound.midi.*;
import ace.datatypes.FeatureDefinition;
import jsymbolic.processing.MIDIIntermediateRepresentations;


/**
 * A feature exractor that finds the difference between the average loudness of 
 * the loudest channel and the average loudness of the other channels that 
 * contain at least one note.
 *
 * <p>No extracted feature values are stored in objects of this class.
 *
 * @author Cory McKay
 */
public class ImportanceOfLoudestVoiceFeature
     extends MIDIFeatureExtractor
{
     /* CONSTRUCTOR ***********************************************************/
     
     
     /**
      * Basic constructor that sets the definition and dependencies (and their
      * offsets) of this feature.
      */
     public ImportanceOfLoudestVoiceFeature()
     {
          String name = "Importance of Loudest Voice";
          String description = "Difference between the average loudness of the loudest\n" +
               "channel and the average loudness of the other channels\n" +
               "that contain at least one note.";
          boolean is_sequential = true;
          int dimensions = 1;
          definition = new FeatureDefinition( name,
               description,
               is_sequential,
               dimensions );
          
          dependencies = null;
          
          offsets = null;
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Extracts this feature from the given MIDI sequence given the other
      * feature values.
      *
      * <p>In the case of this feature, the other_feature_values parameters
      * are ignored.
      *
      * @param sequence			The MIDI sequence to extract the feature
      *                                 from.
      * @param sequence_info		Additional data about the MIDI sequence.
      * @param other_feature_values	The values of other features that are
      *					needed to calculate this value. The
      *					order and offsets of these features
      *					must be the same as those returned by
      *					this class's getDependencies and
      *					getDependencyOffsets methods
      *                                 respectively. The first indice indicates
      *                                 the feature/window and the second
      *                                 indicates the value.
      * @return				The extracted feature value(s).
      * @throws Exception		Throws an informative exception if the
      *					feature cannot be calculated.
      */
     public double[] extractFeature( Sequence sequence,
          MIDIIntermediateRepresentations sequence_info,
          double[][] other_feature_values )
          throws Exception
     {
          double value;
          if (sequence_info != null)
          {
               // Find the number of channels with no note ons
               int silent_count = 0;
               for (int chan = 0; chan < sequence_info.channel_statistics.length; chan++)
                    if (sequence_info.channel_statistics[chan][0] == 0)
                         silent_count++;
               
               // Find the loudest channel
               int max_so_far = 0;
               int loudest_chan = 0;
               for (int chan = 0; chan < sequence_info.channel_statistics.length; chan++)
                    if (sequence_info.channel_statistics[chan][0] != 0)
                         if (sequence_info.channel_statistics[chan][2] > max_so_far)
                         {
                    max_so_far = sequence_info.channel_statistics[chan][2];
                    loudest_chan = chan;
                         }
               double loudest_average = (double) max_so_far;
               
               // Find the average of the other channels and set value
               int number_voices = sequence_info.channel_statistics.length - silent_count;
               if (number_voices < 2)
                    value = 0.0;
               else
               {
                    double[] other_averages = new double[number_voices - 1];
                    int count = 0;
                    for (int chan = 0; chan < sequence_info.channel_statistics.length; chan++)
                         if (sequence_info.channel_statistics[chan][0] != 0 && chan != loudest_chan)
                         {
                         other_averages[count] = (double) sequence_info.channel_statistics[chan][2];
                         count++;
                         }
                    double average = mckay.utilities.staticlibraries.MathAndStatsMethods.getAverage(other_averages);
                    value = loudest_average - average;
               }
          }
          else value = -1.0;
          
          double[] result = new double[1];
          result[0] = value;
          return result;
     }
}