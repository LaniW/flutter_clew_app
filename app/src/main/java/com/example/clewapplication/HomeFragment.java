package com.example.clewapplication;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        Button button = rootView.findViewById(R.id.button100);
        button.setOnClickListener(view -> {
            Intent i = new Intent(getActivity().getApplicationContext(), SingleUseRouteActivity.class);
            getActivity().startActivity(i);
        });

        Button button1 = rootView.findViewById(R.id.button200);
        button1.setOnClickListener(view -> {
            Intent ii = new Intent(getActivity().getApplicationContext(), SaveRouteActivity.class);
            getActivity().startActivity(ii);
        });


        Button button2 = rootView.findViewById(R.id.button300);
        button2.setOnClickListener(view -> {
            Intent iii = new Intent(getActivity().getApplicationContext(), SavedRoutesListActivity.class);
            getActivity().startActivity(iii);
        });

        return inflater.inflate(R.layout.fragment_home,container,false);
    }
}