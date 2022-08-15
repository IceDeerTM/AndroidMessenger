package com.deerdevelop.androidmessenger.ui.calendar;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import com.deerdevelop.androidmessenger.R;
import com.deerdevelop.androidmessenger.databinding.FragmentCalendarBinding;
import com.deerdevelop.androidmessenger.ui.informationdialog.InfoDialogFragment;
import com.deerdevelop.androidmessenger.ui.informationdialog.Toaster;
import java.util.Calendar;

public class CalendarFragment extends Fragment {

    private CardView cardViewPickTimeStart;
    private CardView cardViewPickTimeEnd;
    private CalendarView calendarView;
    private Calendar selectedCalendar;
    private TextView textViewTimeStart, textViewTimeEnd;
    private EditText editTextTitle, editTextDescription;
    private InfoDialogFragment infoDialogFragment = new InfoDialogFragment();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentCalendarBinding binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        selectedCalendar = Calendar.getInstance();
        CardView cardViewAddEvent = binding.cardViewAddEvent;
        calendarView = binding.calendarView;
        cardViewPickTimeStart = binding.cardViewPickTimeStart;
        cardViewPickTimeEnd = binding.cardViewPickTimeEnd;
        textViewTimeStart = binding.textViewTimeStart;
        textViewTimeEnd = binding.textViewTimeEnd;
        editTextTitle = binding.editTextTitle;
        editTextDescription = binding.editTextDescription;

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedCalendar.set(year, month, dayOfMonth);
            /*SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            date = formatter.format(calendar.getTime());
            int mYear = year;
            int mMonth = month;
            int mDay = dayOfMonth;
            String selectedDate = new StringBuilder().append(mMonth + 1)
                    .append("-").append(mDay).append("-").append(mYear)
                    .append(" ").toString();*/
        });


        textViewTimeStart.setText("08:00");
        textViewTimeEnd.setText("18:00");

        cardViewAddEvent.setOnClickListener(cardViewAddEventClicked);
        cardViewPickTimeStart.setOnClickListener(cardViewPickTimeClicked);
        cardViewPickTimeEnd.setOnClickListener(cardViewPickTimeClicked);

        return root;
    }

    View.OnClickListener cardViewAddEventClicked = v -> {
        String description, title;
        description = editTextDescription.getText().toString(); title = editTextTitle.getText().toString();

        if (description.equals("")) description = "default";
        if (title.equals("")) title = "default title";

        int hourStart, minuteStart, hourEnd, minuteEnd;
        String[] str = textViewTimeStart.getText().toString().split(":");
        hourStart = Integer.parseInt(str[0]); minuteStart = Integer.parseInt(str[1]);

        str = textViewTimeEnd.getText().toString().split(":");
        hourEnd = Integer.parseInt(str[0]); minuteEnd = Integer.parseInt(str[1]);

        if (hourStart > hourEnd) {
            Toaster.show(getContext(), "Время начала позже времени окончания", false);
            return;
        }
        else if (hourStart == hourEnd) {
            if (minuteStart > minuteEnd)  {
                Toaster.show(getContext(), "Время начала позже времени окончания", false);
                return;
            }
            else if (minuteStart == minuteEnd) {
                Toaster.show(getContext(), "Время начала равно времени окончания", false);
                return;
            }
        }

        addEvent(title, description, hourStart, minuteStart, hourEnd, minuteEnd);
    };

    View.OnClickListener cardViewPickTimeClicked = v -> {
        TextView textView;

        if (v.getId() == R.id.cardViewPickTimeStart) textView = textViewTimeStart;
        else textView = textViewTimeEnd;

        callTimePicker(textView);
    };

    private void addEvent(String title, String description, int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        Uri calendars = CalendarContract.Calendars.CONTENT_URI;

        try {
            Cursor managedCursor = getContext().getContentResolver().query(calendars, new String[] { "_id", "name" },
                    null, null, null);
            //managedCursor.getColumnNames().length > 1
            if (managedCursor != null && managedCursor.moveToFirst())
            {
                String calName;
                int calID;
                long calGoogle = 0;
                int nameColumn = managedCursor.getColumnIndex("name");
                int idColumn = managedCursor.getColumnIndex("_id");
                do
                {
                    calName = managedCursor.getString(nameColumn);
                    calID = managedCursor.getInt(idColumn);

                    if (calName != null) {
                        if (calName.contains("@gmail.com")) calGoogle = calID;
                    }


                } while (managedCursor.moveToNext());
                managedCursor.close();

                Calendar beginTime = Calendar.getInstance(), endTime = Calendar.getInstance();


                int year = selectedCalendar.get(Calendar.YEAR);
                int month = selectedCalendar.get(Calendar.MONTH);
                int date = selectedCalendar.get(Calendar.DATE);

                beginTime.set(year, month, date, hourStart, minuteStart);
                long startMillis = beginTime.getTimeInMillis();
                endTime.set(year, month, date, hourEnd, minuteEnd);
                long endMillis = endTime.getTimeInMillis();

                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.DTSTART, startMillis);
                values.put(CalendarContract.Events.DTEND, endMillis);
                values.put(CalendarContract.Events.TITLE, title);
                values.put(CalendarContract.Events.DESCRIPTION, description);
                values.put(CalendarContract.Events.CALENDAR_ID, calGoogle);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, "Russia/Yekaterinburg");
                Uri uri = getContext().getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);

// get the event ID that is the last element in the Uri
                long eventID = Long.parseLong(uri.getLastPathSegment());
                Toaster.show(getContext(), "Событие добавлено.", false);
            }
        }
        catch (Exception e) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            String messageInfoDialog = e.getMessage();
            infoDialogFragment.setTextMessage(messageInfoDialog);
            infoDialogFragment.show(manager, "Dialog");
            e.printStackTrace();
        }
    }

    private void callTimePicker(TextView textView) {
        // получаем текущее время
        final Calendar cal = Calendar.getInstance();
        int selectedHour = cal.get(Calendar.HOUR_OF_DAY);
        int selectedMinute = cal.get(Calendar.MINUTE);

        // инициализируем диалог выбора времени текущими значениями
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {

            String hour, minuteStr;
            if (hourOfDay < 10)  hour = "0" + hourOfDay;
            else hour = Integer.toString(hourOfDay);

            if (minute < 10) minuteStr = "0" + minute;
            else minuteStr = Integer.toString(minute);

            String editTextTimeParam = hour + ":" + minuteStr;
            textView.setText(editTextTimeParam);
            }, selectedHour, selectedMinute, false);
        timePickerDialog.show();
    }

}