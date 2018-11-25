package com.shareThegame.STG.Controller;

import com.shareThegame.STG.Model.PaymentHisotry;
import com.shareThegame.STG.Model.SportObject;
import com.shareThegame.STG.Model.TimeTable;
import com.shareThegame.STG.Model.User;
import com.shareThegame.STG.Repository.PaymentHistoryRepository;
import com.shareThegame.STG.Repository.SportObjectReposiotry;
import com.shareThegame.STG.Repository.TimeTableReposiotry;
import com.shareThegame.STG.Repository.UserRepository;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.validation.Valid;
import java.text.ParseException;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public
class TimeTableController {

    @Autowired
    TimeTableReposiotry timeTableReposiotry;
    @Autowired
    SportObjectReposiotry sportObjectReposiotry;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PaymentHistoryRepository paymentHistoryRepository;

    @PostMapping ( value = "/ReserveHall", produces = "application/json", consumes = "application/x-www-form-urlencoded;charset=UTF-8" )
    public @ResponseBody
    String reserve ( @Valid TimeTable timeTable , String end , String start ) throws ParseException {
        List <TimeTable> timeTableList = timeTableReposiotry.findAllBySportobjectid ( timeTable.getSportobjectid ( ) );
        Collections.sort ( timeTableList );

        DateTime postStartRent;
        DateTime postEndRent;
        boolean isFree = true;

        postStartRent = pasreDate ( start );
        Date postStartRentDate = postStartRent.toDate ( );


        postEndRent = pasreDate ( end );
        Date postEndRentDate = postEndRent.toDate ( ); 
        if ( timeTableList.size ( ) > 0 ) {

            isFree = chceckAvalabile ( timeTableList , postStartRentDate , postEndRentDate );

        } else{
            isFree = true;
        }


//


        JSONObject jsonObject = new JSONObject ( );
        if ( isFree == false ) {
            jsonObject.put ( "status" , "hala zjeta w tym terminie" );
        } else{
            String username = userAuth ( );

            isFree = true;

            User user = userRepository.findByEmail ( username );
            SportObject sportObject = sportObjectReposiotry.getOne ( timeTable.getSportobjectid ( ) );

            //set timetable
            timeTable.setRenterid ( user.getId ( ) );
            timeTable.setStartrent ( postStartRent.toDate ( ) );
            timeTable.setEndrend ( postEndRent.toDate ( ) );
            double time = postEndRent.toDate ( ).getTime ( ) - postStartRent.toDate ( ).getTime ( );
            time /= 3600000;
            timeTable.setPrice ( ( time * sportObject.getRentprice ( ) ) );
            timeTableReposiotry.save ( timeTable );

            PaymentHisotry paymentHisotry = new PaymentHisotry ( );
            paymentHisotry.setStartrent ( postStartRent.toDate ( ) );
            paymentHisotry.setExprrent ( postEndRent.toDate ( ) );
            paymentHisotry.setStautsofpayment ( false );
            paymentHisotry.setSportobjectid ( sportObject.getId ( ) );
            paymentHisotry.setUserid ( user.getId ( ) );
            paymentHisotry.setSportObject ( sportObject );
            paymentHisotry.setCost ( time * sportObject.getRentprice ( ) );
            paymentHisotry.setUser ( user );
            paymentHistoryRepository.save ( paymentHisotry );


            user.getPaymentHisotries ( ).add ( paymentHisotry );
            sportObject.getPaymentHisotries ( ).add ( paymentHisotry );
            userRepository.save ( user );
            sportObjectReposiotry.save ( sportObject );


            sportObject.getTimeTable ( ).add ( timeTable );
            sportObjectReposiotry.save ( sportObject );

            jsonObject.put ( "status" , "hala wolona w tym temrinie dokonano rezerwacji" );
        }
        return jsonObject.toString ( );

    }

    @PostMapping ( value = "/ChangeReservation", produces = "application/json", consumes = "application/x-www-form-urlencoded;charset=UTF-8" )
    public @ResponseBody
    String changeReservation ( Long sportobjectid , String end , String start , String newstart , String newend ) {
        boolean isFree = true;
        DateTime oldPostStartRent;
        DateTime oldPostEndRent;
        DateTime newPostStartRent;
        DateTime newPostEndRent;

        oldPostStartRent = pasreDate ( start );
        Date oldPostStartRentDate = oldPostStartRent.toDate ( );

        oldPostEndRent = pasreDate ( end );
        Date oldPostEndRentDate = oldPostEndRent.toDate ( );

        newPostStartRent = pasreDate ( newstart );
        Date newPostStartRentDate = newPostStartRent.toDate ( );

        newPostEndRent = pasreDate ( newend );
        Date newPostEndRentDate = newPostEndRent.toDate ( );

        ArrayList <TimeTable> timeTableList = timeTableReposiotry.findAllBySportobjectid ( sportobjectid );
        Collections.sort ( timeTableList );
        if ( timeTableList.size ( ) > 0 ) {
            isFree = chceckAvalabile ( timeTableList , newPostStartRentDate , newPostEndRentDate );

        } else{
            isFree = true;
        }


        JSONObject jsonObject = new JSONObject ( );
        if ( isFree == false ) {
            jsonObject.put ( "status" , "hala zjeta w tym terminie" );
        } else{

            TimeTable timeTableToChange = timeTableReposiotry.findByStartrentAndEndrendAndSportobjectid ( oldPostStartRentDate , oldPostEndRentDate , sportobjectid );
            if ( timeTableToChange != null ) {

//iniit
                String username = userAuth ( );
                User user = userRepository.findByEmail ( username );
                SportObject sportObject = sportObjectReposiotry.getOne ( timeTableToChange.getSportobjectid ( ) );
                PaymentHisotry paymentHisotry = paymentHistoryRepository.findBySportobjectidAndAndStartrentAndExprrent ( sportobjectid , oldPostStartRentDate , oldPostEndRentDate );
                // System.out.println (oldPostStartRentDate);

                //delete old reserv
                sportObject.getTimeTable ( ).remove ( timeTableToChange );
                sportObject.getPaymentHisotries ( ).remove ( paymentHisotry );
                user.getPaymentHisotries ( ).remove ( paymentHisotry );


                userRepository.save ( user );
                sportObjectReposiotry.save ( sportObject );

                paymentHistoryRepository.delete ( paymentHisotry );
                timeTableReposiotry.delete ( timeTableToChange );

                TimeTable timeTable1 = new TimeTable ( );
                timeTable1.setStartrent ( newPostStartRentDate );
                timeTable1.setEndrend ( newPostEndRentDate );
                timeTable1.setSportobjectid ( sportobjectid );
                timeTable1.setRenterid ( user.getId ( ) );
                double time = newPostEndRent.toDate ( ).getTime ( ) - newPostStartRent.toDate ( ).getTime ( );
                time /= 3600000;
                timeTable1.setPrice ( time * sportObject.getRentprice ( ) );
                timeTable1.setStartrent ( newPostStartRentDate );
                timeTable1.setEndrend ( newPostEndRentDate );
                PaymentHisotry paymentHisotry1 = new PaymentHisotry ( );
                paymentHisotry1.setStartrent ( newPostStartRentDate );
                paymentHisotry1.setExprrent ( newPostEndRentDate );
                paymentHisotry1.setStautsofpayment ( false );
                paymentHisotry1.setSportobjectid ( sportObject.getId ( ) );
                paymentHisotry1.setUserid ( user.getId ( ) );
                paymentHisotry1.setSportObject ( sportObject );
                paymentHisotry1.setCost ( time * sportObject.getRentprice ( ) );
                paymentHisotry1.setUser ( user );
                paymentHisotry1.setUserid ( user.getId ( ) );
                paymentHisotry1.setSportobjectid ( sportobjectid );

                paymentHistoryRepository.save ( paymentHisotry1 );

                //  System.out.println ( paymentHisotry.getId ( ) );
                user.getPaymentHisotries ( ).add ( paymentHisotry1 );
                sportObject.getPaymentHisotries ( ).add ( paymentHisotry1 );

                userRepository.save ( user );
                timeTableReposiotry.save ( timeTable1 );
                sportObjectReposiotry.save ( sportObject );


                //  System.out.println (time );


                sportObject.getTimeTable ( ).add ( timeTable1 );
                //System.out.println ( "id " + timeTable1.getId ( ) );
                sportObjectReposiotry.save ( sportObject );


                jsonObject.put ( "status" , "udana zmiana rezerwacji " );

            } else{
                jsonObject.put ( "status" , "blad" );

            }

        }
        return jsonObject.toString ( );
    }


    private
    DateTime pasreDate ( String date ) {
        int year;
        int month;
        int day;
        int hour;
        int minutes;
        int sec;
        //string z data z posta


        year = Integer.parseInt ( date.substring ( 0 , date.length ( ) - 17 ) );
        month = Integer.parseInt ( date.substring ( 5 , date.length ( ) - 14 ) );
        day = Integer.parseInt ( date.substring ( 8 , date.length ( ) - 11 ) );
        hour = Integer.parseInt ( date.substring ( 11 , date.length ( ) - 8 ) );
        minutes = Integer.parseInt ( date.substring ( 14 , date.length ( ) - 5 ) );
        sec = Integer.parseInt ( date.substring ( 18 , date.length ( ) - 2 ) );

        return new DateTime ( year , month , day , hour , minutes , sec );
    }

    private
    String userAuth ( ) {

        String username;
        try {
            org.springframework.security.core.userdetails.User currentUser =
                    ( org.springframework.security.core.userdetails.User ) SecurityContextHolder.getContext ( ).getAuthentication ( ).getPrincipal ( );
            username = currentUser.getUsername ( );
            return username;
        } catch ( ClassCastException e ) {
            username = "anonymousUser";
            return "{}";
        }
    }

    private
    Boolean chceckAvalabile ( List <TimeTable> timeTableList , Date postStartRentDate , Date postEndRentDate ) {
        boolean isFree = true;
        if ( timeTableList.size ( ) > 1 ) {
            System.out.println ( "tutaj" );
            for (int i = 0; i < timeTableList.size ( ) - 1; i++) {
                System.out.println ( timeTableList.get ( i ).getStartrent ( ) + " " + timeTableList.get ( i ).getEndrend ( ) + "      " + postStartRentDate + " " + postEndRentDate );
                if ( ( timeTableList.get ( i ).getStartrent ( ).getTime ( ) == postStartRentDate.getTime ( ) )
                        || ( postStartRentDate.getTime ( ) >= timeTableList.get ( i ).getStartrent ( ).getTime ( )
                        && postStartRentDate.getTime ( ) < timeTableList.get ( i ).getEndrend ( ).getTime ( ) )
                        || ( postEndRentDate.getTime ( ) > timeTableList.get ( i + 1 ).getStartrent ( ).getTime ( ) &&
                        postEndRentDate.getTime ( ) <= timeTableList.get ( i + 1 ).getEndrend ( ).getTime ( )
                ) ) {

                    isFree = false;
                    break;
                }
            }
        } else{


//           for (TimeTable item : timeTableList) {
//               System.out.println ("tutaj1" );
//               if ( item.getStartrent ( ).getTime ( ) == postStartRentDate.getTime ( ) || ( postStartRentDate.getTime ( ) >= item.getStartrent ( ).getTime ( ) && postStartRentDate.getTime ( ) < item.getEndrend ( ).getTime ( ) ) ) {
//
//                   isFree = false;
//                    break;
//               }
//
//           }
//       }

            if ( timeTableList.get ( 0 ).getStartrent ( ).getTime ( ) == postStartRentDate.getTime ( ) ||
                    ( postStartRentDate.getTime ( ) >= timeTableList.get ( 0 ).getStartrent ( ).getTime ( ) && postStartRentDate.getTime ( ) < timeTableList.get ( 0 ).getEndrend ( ).getTime ( ) ) ) {

                isFree = false;
            }


        }
        return isFree;
    }
}
