import { Component, OnInit } from '@angular/core';
import { Event } from '../model/event';
import { ISubscription } from "rxjs/Subscription";
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-root',
  templateUrl: 'event-service.component.html',
  styleUrls: ['event-service.component.css'],
  providers: [],
})
export class EventComponent implements OnInit {

    event: Event;
    _eventSubscription: ISubscription;

    serverUrl = environment.apiEndpoint; // e.g. http://123abc.ngrok.io/socket
    stompClient: any;

      connect() {
          const socket = new SockJS(this.serverUrl);
          this.stompClient = Stomp.over(socket);

          const _this = this;
          this.stompClient.connect({}, function (frame: any) {
            console.log('Connected: ' + frame);

            _this.stompClient.subscribe('/clock/event', function (data: any) {
              _this.event = JSON.parse(data.body);
            });
          });
        }

    constructor() { }

    ngOnInit() {
        this.event = new Event();
        this.event.tenant="N/A";
        this.event.eventTime="N/A";
        this.connect();
    }

    ngOnDestroy() {
        this._eventSubscription.unsubscribe();
    }

}
