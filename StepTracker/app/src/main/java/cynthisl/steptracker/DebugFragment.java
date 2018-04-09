package cynthisl.steptracker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;


/**
 * A simple {@link Fragment} subclass.
 */
public class DebugFragment extends Fragment {

    TextView _tv_x, _tv_y, _tv_z, _tv_androidCount, _tv_stepCount;
    Button _bReset;

    private LineGraphSeries<DataPoint> _graph_x;
    private LineGraphSeries<DataPoint> _graph_y;
    private LineGraphSeries<DataPoint> _graph_z;
    private LineGraphSeries<DataPoint> _graph_mag;
    private PointsGraphSeries<DataPoint> _graph_peaks;




    public DebugFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_debug, container, false);
    }

}
