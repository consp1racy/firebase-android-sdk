# Copyright 2023 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the 'License');
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an 'AS IS' BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import github
import json
import datetime
import argparse
import logging
import workflow_information


'''A utility collecting ci_test.yml workflow failure logs.

Usage:

  python workflow_information.py --token ${github_toke} --workflow_name ${workflow_name}

'''

def main(): 
  logging.getLogger().setLevel(logging.INFO)

  args = parse_cmdline_args()

  gh = github.GitHub('firebase', 'firebase-android-sdk')

  monthly_summary = {}
  first_day_this_month = datetime.date.today().replace(day=1)
  first_day_in_month = first_day_this_month
  for i in range(6):
    last_day_in_month = first_day_in_month - datetime.timedelta(days=1)
    first_day_in_month = last_day_in_month.replace(day=1)
    first_day_in_month = first_day_in_month

    from_time = datetime.datetime.combine(first_day_in_month, datetime.time.min)
    to_time = datetime.datetime.combine(last_day_in_month, datetime.time.max)
    created = from_time.strftime('%Y-%m-%dT%H:%M:%SZ') + '..' + to_time.strftime('%Y-%m-%dT%H:%M:%SZ')

    workflow_summary = workflow_information.get_workflow_summary(gh=gh, token=args.token, created=created, workflow_name='ci_tests.yml', event='push', branch='master')
    monthly_summary[first_day_this_month] = {
      'failure_rate': float(workflow_summary['failure_count']/workflow_summary['total_count']),
      'total_count': workflow_summary['total_count'],
      'success_count': workflow_summary['success_count'],
      'failure_count': workflow_summary['failure_count'],
      'failure_jobs':{}
      }
    job_summary = workflow_information.get_job_summary(workflow_summary)
    for job_name in job_summary:
      job = job_summary[job_name]
      if job['failure_rate'] > 0:
        monthly_summary[first_day_this_month]['failure_jobs'][job_name] = {
            'failure_rate': job['failure_rate'],
            'total_count': job['total_count'],
            'success_count': job['success_count'],
            'failure_count': job['failure_count'],
        }

  monthly_summary = dict(sorted(monthly_summary.items(), reverse=True))
  logging.info(monthly_summary)

def parse_cmdline_args():
  parser = argparse.ArgumentParser()
  parser.add_argument('-t', '--token', required=True, help='GitHub access token')

  args = parser.parse_args()
  return args


if __name__ == '__main__':
  main()