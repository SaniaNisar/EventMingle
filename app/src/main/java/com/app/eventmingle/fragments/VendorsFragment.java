package com.app.eventmingle.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.eventmingle.R;
import com.app.eventmingle.adapters.VendorAdapter;
import com.app.eventmingle.models.Vendor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VendorsFragment extends Fragment {

    private RecyclerView recyclerView;
    private String eventId;
    private VendorAdapter vendorAdapter;
    private List<Vendor> vendorList;
    private DatabaseReference vendorRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendors, container, false);

        recyclerView = view.findViewById(R.id.vendorsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("EventPrefs", Context.MODE_PRIVATE);
        eventId = sharedPreferences.getString("eventId", null); // Assuming eventId is stored as string


        vendorList = new ArrayList<>();
        vendorAdapter = new VendorAdapter(vendorList, vendor -> {
            // Handle "Add to Event" button click
            addVendorToEvent(vendor);
        });
        recyclerView.setAdapter(vendorAdapter);

        vendorRef = FirebaseDatabase.getInstance().getReference("vendors");

        // OPTIONAL: Populate sample data ONCE
        populateSampleVendors(); // ⚠️ Comment out after first run

        fetchVendorsFromFirebase();

        return view;
    }

    private void addVendorToEvent(Vendor vendor) {
        if (eventId == null) {
            Toast.makeText(getContext(), "Event ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events").child(eventId);

        // Add the vendor by vendorId to the event
        eventRef.child("vendors").child(vendor.getVendorId()).setValue(vendor)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Vendor added to event", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add vendor: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void fetchVendorsFromFirebase() {
        vendorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vendorList.clear();
                for (DataSnapshot vendorSnapshot : snapshot.getChildren()) {
                    Vendor vendor = vendorSnapshot.getValue(Vendor.class);
                    if (vendor != null) {
                        vendorList.add(vendor);
                    }
                }
                vendorAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load vendors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateSampleVendors() {
        Vendor v1 = new Vendor(
                "Elegant Events",
                "Premium event management services.",
                "03001234567",
                "Lahore, Pakistan",
                "elegant@events.com",
                "Event Planning"
        );

        Vendor v2 = new Vendor(
                "Cater Kings",
                "Delicious food for your special day.",
                "03119876543",
                "Islamabad, Pakistan",
                "cater@kings.com",
                "Catering"
        );

        Vendor v3 = new Vendor(
                "Decor Masters",
                "We create breathtaking wedding setups.",
                "03221234567",
                "Karachi, Pakistan",
                "decor@masters.com",
                "Decorations"
        );

        vendorRef.push().setValue(v1);
        vendorRef.push().setValue(v2);
        vendorRef.push().setValue(v3);
    }
}
