package com.example.app.adapters;

import static org.junit.Assert.assertEquals;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.test.core.app.ApplicationProvider;

import com.example.app.R;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class UrlImageSliderAdapterTest {
    @Test
    public void itemCount_matchesInputSize() {
        List<String> urls = Arrays.asList("http://example/a.jpg","http://example/b.jpg");
        List<String> titles = Arrays.asList("A","B");
        List<String> ctas = Arrays.asList("Xem","Xem");
        UrlImageSliderAdapter adapter = new UrlImageSliderAdapter(urls, titles, ctas, v -> {});
        assertEquals(2, adapter.getItemCount());
    }
}
