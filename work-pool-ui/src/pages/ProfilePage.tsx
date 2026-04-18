import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { User, MapPin, Star, Edit2, CheckCircle } from 'lucide-react';

const INDIA_STATES = [
  'Andhra Pradesh', 'Assam', 'Bihar', 'Chhattisgarh', 'Delhi', 'Gujarat',
  'Haryana', 'Karnataka', 'Kerala', 'Maharashtra', 'Rajasthan', 'Tamil Nadu',
  'Telangana', 'Uttar Pradesh', 'West Bengal',
];

const SKILL_SUGGESTIONS = [
  'Plumbing', 'Electrical', 'Carpentry', 'Painting', 'Cleaning', 'Cooking',
  'Teaching', 'Driving', 'Photography', 'Marketing', 'Coding', 'Accounting',
  'Gardening', 'Tailoring', 'Welding', 'AC Repair', 'Computer Repair',
];

const ProfilePage: React.FC = () => {
  const { user, updateUser } = useAuthStore();
  const queryClient = useQueryClient();
  const [editing, setEditing] = useState(false);
  const [skillInput, setSkillInput] = useState('');
  const [form, setForm] = useState({
    fullName: user?.fullName ?? '',
    phoneNumber: user?.phoneNumber ?? '',
    bio: user?.bio ?? '',
    serviceRadiusKm: user?.serviceRadiusKm ?? 20,
    skills: user?.skills ? [...user.skills] : [],
    location: user?.location ?? { city: '', district: '', state: '', pincode: '', latitude: 0, longitude: 0 },
  });

  const { data, isLoading } = useQuery({
    queryKey: ['profile', 'me'],
    queryFn: () => userApi.getMe(),
  });

  const mutation = useMutation({
    mutationFn: () => userApi.updateProfile(form),
    onSuccess: (res) => {
      if (res.data.data) {
        updateUser(res.data.data);
      }
      queryClient.invalidateQueries({ queryKey: ['profile', 'me'] });
      setEditing(false);
    },
  });

  const profile = data?.data?.data ?? user;

  if (isLoading) return <LoadingSpinner className="py-20" />;

  const addSkill = (s: string) => {
    const sk = s.trim();
    if (sk && !form.skills.includes(sk)) setForm({ ...form, skills: [...form.skills, sk] });
    setSkillInput('');
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 space-y-6">
      {/* Profile card */}
      <div className="card p-6 space-y-4">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-4">
            {profile?.profileImageUrl ? (
              <img src={profile.profileImageUrl} alt={profile.fullName} className="w-16 h-16 rounded-full object-cover" />
            ) : (
              <div className="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center">
                <User className="w-8 h-8 text-blue-600" />
              </div>
            )}
            <div>
              <h1 className="text-xl font-bold text-gray-900">{profile?.fullName}</h1>
              <p className="text-sm text-gray-500">{profile?.email}</p>
              {(profile?.averageRating ?? 0) > 0 && (
                <div className="flex items-center gap-1 mt-1">
                  <Star className="w-4 h-4 text-yellow-500 fill-yellow-500" />
                  <span className="text-sm font-medium">{(profile?.averageRating ?? 0).toFixed(1)}</span>
                  <span className="text-xs text-gray-400">({profile?.totalRatings ?? 0} ratings)</span>
                </div>
              )}
            </div>
          </div>
          <button onClick={() => setEditing(!editing)} className="btn-secondary text-sm py-1.5 px-3">
            <Edit2 className="w-3.5 h-3.5 inline mr-1" />{editing ? 'Cancel' : 'Edit'}
          </button>
        </div>

        {profile?.aadhaarVerification === 'VERIFIED' && (
          <div className="flex items-center gap-1 text-green-600 text-sm">
            <CheckCircle className="w-4 h-4" /> Aadhaar Verified
          </div>
        )}

        {profile?.location && (
          <div className="flex items-center gap-1 text-sm text-gray-500">
            <MapPin className="w-4 h-4" />
            {profile.location.city}, {profile.location.district}, {profile.location.state}
            {profile.serviceRadiusKm > 0 && ` · ${profile.serviceRadiusKm} km radius`}
          </div>
        )}

        {profile?.bio && <p className="text-sm text-gray-700">{profile.bio}</p>}

        {profile?.skills && profile.skills.length > 0 && (
          <div className="flex flex-wrap gap-1">
            {profile.skills.map((s: string) => (
              <span key={s} className="bg-blue-50 text-blue-700 text-xs px-2 py-0.5 rounded-full">{s}</span>
            ))}
          </div>
        )}
      </div>

      {/* Edit form */}
      {editing && (
        <div className="card p-6 space-y-4">
          <h2 className="font-semibold text-gray-900">Edit Profile</h2>
          <form onSubmit={(e) => { e.preventDefault(); mutation.mutate(); }} className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
                <input className="input" value={form.fullName}
                  onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
                <input className="input" value={form.phoneNumber}
                  onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Bio</label>
              <textarea className="input" rows={3} value={form.bio}
                onChange={(e) => setForm({ ...form, bio: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Service Radius (km)</label>
              <input className="input" type="number" min={1} max={200} value={form.serviceRadiusKm}
                onChange={(e) => setForm({ ...form, serviceRadiusKm: Number(e.target.value) })} />
            </div>

            {/* Location */}
            <fieldset className="space-y-2">
              <legend className="text-sm font-medium text-gray-700">Location</legend>
              <div className="grid grid-cols-2 gap-2">
                <input className="input" placeholder="City" value={form.location.city}
                  onChange={(e) => setForm({ ...form, location: { ...form.location, city: e.target.value } })} />
                <input className="input" placeholder="District" value={form.location.district}
                  onChange={(e) => setForm({ ...form, location: { ...form.location, district: e.target.value } })} />
                <select className="input" value={form.location.state}
                  onChange={(e) => setForm({ ...form, location: { ...form.location, state: e.target.value } })}>
                  <option value="">Select State</option>
                  {INDIA_STATES.map((s) => <option key={s} value={s}>{s}</option>)}
                </select>
                <input className="input" placeholder="Pincode" value={form.location.pincode ?? ''}
                  onChange={(e) => setForm({ ...form, location: { ...form.location, pincode: e.target.value } })} />
              </div>
            </fieldset>

            {/* Skills */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Skills</label>
              <div className="flex gap-2 mb-2">
                <input className="input flex-1" placeholder="Add skill..."
                  value={skillInput} onChange={(e) => setSkillInput(e.target.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addSkill(skillInput); }}} />
                <button type="button" onClick={() => addSkill(skillInput)} className="btn-secondary text-sm px-3">Add</button>
              </div>
              <div className="flex flex-wrap gap-1 mb-2">
                {form.skills.map((s) => (
                  <span key={s} className="bg-blue-100 text-blue-700 text-xs px-2 py-0.5 rounded-full flex items-center gap-1">
                    {s}
                    <button type="button" onClick={() => setForm({ ...form, skills: form.skills.filter((x) => x !== s) })}>×</button>
                  </span>
                ))}
              </div>
              <div className="flex flex-wrap gap-1">
                {SKILL_SUGGESTIONS.filter((s) => !form.skills.includes(s)).map((s) => (
                  <button type="button" key={s} onClick={() => addSkill(s)}
                    className="text-xs border rounded-full px-2 py-0.5 text-gray-600 hover:bg-gray-50">+ {s}</button>
                ))}
              </div>
            </div>

            <button type="submit" disabled={mutation.isPending} className="btn-primary w-full">
              {mutation.isPending ? <LoadingSpinner size="sm" className="inline" /> : 'Save Changes'}
            </button>
          </form>
        </div>
      )}

      {/* My tasks shortcuts */}
      <div className="grid grid-cols-2 gap-4">
        <a href="/tasks/my/published" className="card p-4 text-center hover:shadow-md transition-shadow cursor-pointer">
          <p className="font-semibold text-gray-900">My Posted Tasks</p>
          <p className="text-xs text-gray-500 mt-1">Tasks you published</p>
        </a>
        <a href="/tasks/my/assigned" className="card p-4 text-center hover:shadow-md transition-shadow cursor-pointer">
          <p className="font-semibold text-gray-900">My Assigned Tasks</p>
          <p className="text-xs text-gray-500 mt-1">Tasks you're working on</p>
        </a>
      </div>
    </div>
  );
};

export default ProfilePage;
