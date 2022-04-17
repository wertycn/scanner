// @ts-ignore
import { baseURL } from './config';
// @ts-ignore
import axios from "../axios";

export function queryConfig() {
  return axios<any>({
    baseURL,
    url: '/configures',
    method: "GET"
  });
}

export function updateConfig(type: string, parameter: any) {
  return axios<any>({
    baseURL,
    url: `/configures/types/${type}`,
    method: 'POST',
    data: parameter
  })
}
